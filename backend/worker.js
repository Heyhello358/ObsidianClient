// Obsidian Client backend (Cloudflare Worker + D1).
// - Players prove who they are through Mojang (joinServer / hasJoined), so nobody can fake a UUID.
// - Only ADMIN_UUID can change roles and colours.
const ROLES = ["member", "media", "staff", "high_staff", "owner"];
const ONLINE_MS = 10 * 60 * 1000;   // a player counts as online for 10 min after their last heartbeat
const TOKEN_MS = 6 * 3600 * 1000;
let ready = false;

const json = (obj, status = 200) => new Response(JSON.stringify(obj), {
	status, headers: { "content-type": "application/json", "cache-control": "no-store" },
});
const norm = (s) => { s = String(s || "").toLowerCase().replace(/-/g, ""); return /^[0-9a-f]{32}$/.test(s) ? s : null; };
// returns "rrggbb", null (no colour) or undefined (invalid)
const okColor = (c) => (c == null || c === "") ? null : (/^#?[0-9a-fA-F]{6}$/.test(c) ? c.replace("#", "").toLowerCase() : undefined);
const b64u = (bytes) => btoa(String.fromCharCode(...bytes)).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
const adminId = (env) => norm(env.ADMIN_UUID);

async function init(env) {
	if (ready) return;
	await env.DB.batch([
		env.DB.prepare("CREATE TABLE IF NOT EXISTS meta (k TEXT PRIMARY KEY, v TEXT)"),
		env.DB.prepare("CREATE TABLE IF NOT EXISTS presence (uuid TEXT PRIMARY KEY, name TEXT, seen INTEGER)"),
		env.DB.prepare("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, role TEXT NOT NULL DEFAULT 'member', color TEXT, name TEXT)"),
		env.DB.prepare("CREATE TABLE IF NOT EXISTS used (sid TEXT PRIMARY KEY, t INTEGER)"),
	]);
	ready = true;
}

async function secret(env) {
	let r = await env.DB.prepare("SELECT v FROM meta WHERE k='secret'").first();
	if (!r) {
		const v = [...crypto.getRandomValues(new Uint8Array(32))].map((x) => x.toString(16).padStart(2, "0")).join("");
		await env.DB.prepare("INSERT OR IGNORE INTO meta (k, v) VALUES ('secret', ?)").bind(v).run();
		r = await env.DB.prepare("SELECT v FROM meta WHERE k='secret'").first();
	}
	return r.v;
}

async function sign(env, data) {
	const key = await crypto.subtle.importKey("raw", new TextEncoder().encode(await secret(env)),
		{ name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
	return b64u(new Uint8Array(await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(data))));
}

async function makeToken(env, uuid, name) {
	const exp = Date.now() + TOKEN_MS;
	const payload = btoa(JSON.stringify({ u: uuid, n: name, e: exp })).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
	return { token: payload + "." + await sign(env, payload), exp };
}

async function auth(env, req) {
	const h = req.headers.get("authorization") || "";
	const t = h.startsWith("Bearer ") ? h.slice(7) : "";
	const [payload, sig] = t.split(".");
	if (!payload || !sig || sig !== await sign(env, payload)) return null;
	try {
		const d = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
		if (!norm(d.u) || d.e < Date.now()) return null;
		return { uuid: d.u, name: d.n };
	} catch { return null; }
}

async function verifyJoin(env, username, serverId, body) {
	if (env.MOCK_AUTH === "1") return { id: body.mockUuid, name: username }; // local testing only (.dev.vars)
	const r = await fetch("https://sessionserver.mojang.com/session/minecraft/hasJoined?username="
		+ encodeURIComponent(username) + "&serverId=" + serverId);
	return r.status === 200 ? await r.json() : null;
}

async function roleOf(env, uuid) {
	const row = await env.DB.prepare("SELECT role, color FROM players WHERE uuid=?").bind(uuid).first();
	return { role: uuid === adminId(env) ? "owner" : (row?.role || "member"), color: row?.color || null };
}

async function readBody(req) { try { return await req.json(); } catch { return {}; } }

export default {
	async fetch(req, env) {
		try {
			await init(env);
			const url = new URL(req.url), p = url.pathname, m = req.method;

			if (p === "/") return json({ ok: true, service: "obsidian-backend" });

			if (p === "/login" && m === "POST") {
				const b = await readBody(req);
				if (!/^[A-Za-z0-9_]{1,16}$/.test(b.username || "") || !/^[0-9a-f]{16,64}$/.test(b.serverId || "")) return json({ error: "bad request" }, 400);
				const now = Date.now();
				await env.DB.prepare("DELETE FROM used WHERE t < ?").bind(now - 600000).run();
				const fresh = await env.DB.prepare("INSERT OR IGNORE INTO used (sid, t) VALUES (?, ?)").bind(b.serverId, now).run();
				if (!fresh.meta.changes) return json({ error: "replay" }, 409);
				const prof = await verifyJoin(env, b.username, b.serverId, b);
				const uuid = prof && norm(prof.id);
				if (!uuid) return json({ error: "not verified by Mojang" }, 401);
				await env.DB.prepare("INSERT INTO presence (uuid, name, seen) VALUES (?,?,?) ON CONFLICT(uuid) DO UPDATE SET name=excluded.name, seen=excluded.seen")
					.bind(uuid, prof.name, now).run();
				await env.DB.prepare("DELETE FROM presence WHERE seen < ?").bind(now - 86400000).run();
				const { token, exp } = await makeToken(env, uuid, prof.name);
				const r = await roleOf(env, uuid);
				return json({ token, exp, uuid, role: r.role, color: r.color, admin: uuid === adminId(env) });
			}

			if (p === "/tags" && m === "GET") {
				const ids = (url.searchParams.get("u") || "").split(",").map(norm).filter(Boolean).slice(0, 60);
				if (!ids.length) return json({});
				const ph = ids.map(() => "?").join(",");
				const rows = await env.DB.prepare(
					`SELECT p.uuid AS uuid, pl.role AS role, pl.color AS color FROM presence p LEFT JOIN players pl ON pl.uuid = p.uuid WHERE p.uuid IN (${ph}) AND p.seen > ?`
				).bind(...ids, Date.now() - ONLINE_MS).all();
				const out = {};
				for (const r of rows.results) out[r.uuid] = { r: r.uuid === adminId(env) ? "owner" : (r.role || "member"), c: r.color || null };
				return json(out);
			}

			// everything below needs a verified session
			const me = await auth(env, req);
			if (!me) return json({ error: "unauthorized" }, 401);
			const uuid = norm(me.uuid);

			if (p === "/heartbeat" && m === "POST") {
				await env.DB.prepare("INSERT INTO presence (uuid, name, seen) VALUES (?,?,?) ON CONFLICT(uuid) DO UPDATE SET name=excluded.name, seen=excluded.seen")
					.bind(uuid, me.name, Date.now()).run();
				const r = await roleOf(env, uuid);
				return json({ ok: true, role: r.role, color: r.color });
			}

			// ---- admin only ----
			if (p.startsWith("/admin/")) {
				if (uuid !== adminId(env)) return json({ error: "forbidden" }, 403);

				if (p === "/admin/list" && m === "GET") {
					const stored = (await env.DB.prepare("SELECT uuid, role, color, name FROM players LIMIT 300").all()).results;
					const online = (await env.DB.prepare("SELECT uuid, name FROM presence WHERE seen > ? LIMIT 300").bind(Date.now() - ONLINE_MS).all()).results;
					const map = new Map();
					for (const s of stored) map.set(s.uuid, { uuid: s.uuid, name: s.name, role: s.role, color: s.color, online: false });
					for (const o of online) {
						const e = map.get(o.uuid) || { uuid: o.uuid, role: "member", color: null };
						e.name = o.name || e.name; e.online = true; map.set(o.uuid, e);
					}
					return json({ players: [...map.values()] });
				}

				if (p === "/admin/set" && m === "POST") {
					const b = await readBody(req);
					const u = norm(b.uuid), c = okColor(b.color);
					if (!u || !ROLES.includes(b.role) || c === undefined) return json({ error: "bad request" }, 400);
					const role = u === adminId(env) ? "owner" : b.role;
					const pr = await env.DB.prepare("SELECT name FROM presence WHERE uuid=?").bind(u).first();
					await env.DB.prepare("INSERT INTO players (uuid, role, color, name) VALUES (?,?,?,?) ON CONFLICT(uuid) DO UPDATE SET role=excluded.role, color=excluded.color, name=COALESCE(excluded.name, players.name)")
						.bind(u, role, c, pr?.name || null).run();
					return json({ ok: true });
				}

				if (p === "/admin/remove" && m === "POST") {
					const u = norm((await readBody(req)).uuid);
					if (!u) return json({ error: "bad request" }, 400);
					await env.DB.prepare("DELETE FROM players WHERE uuid=?").bind(u).run();
					return json({ ok: true });
				}
			}
			return json({ error: "not found" }, 404);
		} catch (e) {
			return json({ error: "server error" }, 500);
		}
	},
};
