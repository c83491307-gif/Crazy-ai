import http from 'node:http';
import { readFile, writeFile, mkdir } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
const ROOT=path.dirname(fileURLToPath(import.meta.url));
const DATA_DIR=process.env.DATA_DIR||path.join(ROOT,'data');
const DB=path.join(DATA_DIR,'instances.json');
const HOST=process.env.HOST||'127.0.0.1';
const PORT=Number(process.env.PORT||8787);
await mkdir(DATA_DIR,{recursive:true});
async function readDb(){try{const d=JSON.parse(await readFile(DB,'utf8'));return Array.isArray(d.instances)?d:{instances:[]}}catch{return {instances:[]}}}
let writeQueue=Promise.resolve();
function send(res,status,body,type='application/json; charset=utf-8'){res.writeHead(status,{'Content-Type':type,'X-Content-Type-Options':'nosniff','Cache-Control':'no-store','Content-Security-Policy':"default-src 'self' 'unsafe-inline'; connect-src 'self'; img-src 'self' data:; base-uri 'none'; frame-ancestors 'none'"});res.end(body)}
const server=http.createServer(async(req,res)=>{const url=new URL(req.url,'http://localhost');
if(req.method==='GET'&&url.pathname==='/healthz')return send(res,200,JSON.stringify({status:'ok',service:'creazzy-space'}));
if(url.pathname==='/api/instances'&&req.method==='GET')return send(res,200,JSON.stringify(await readDb()));
if(url.pathname==='/api/instances'&&req.method==='PUT'){let raw='';req.on('data',chunk=>{raw+=chunk;if(raw.length>1_000_000){send(res,413,JSON.stringify({error:'payload_too_large'}));req.destroy()}});req.on('end',async()=>{try{const data=JSON.parse(raw||'{}');if(!Array.isArray(data.instances)||data.instances.length>5000)throw Error('invalid_instances');const seen=new Set();for(const x of data.instances){if(!x||typeof x.id!=='string'||typeof x.name!=='string'||x.name.length>70||seen.has(x.id))throw Error('invalid_instance');seen.add(x.id)}const clean={instances:data.instances.map(x=>({id:x.id,name:x.name,app:String(x.app||'').slice(0,90),category:String(x.category||'أخرى').slice(0,40),note:String(x.note||'').slice(0,180),url:typeof x.url==='string'&&/^https?:\/\//i.test(x.url)?x.url.slice(0,2000):'',active:!!x.active,createdAt:Number(x.createdAt)||Date.now()}))};writeQueue=writeQueue.then(()=>writeFile(DB,JSON.stringify(clean,null,2)));await writeQueue;send(res,200,JSON.stringify({ok:true,count:clean.instances.length}))}catch(e){send(res,400,JSON.stringify({error:'invalid_payload'}))}});return}
if(req.method==='GET'&&(url.pathname==='/'||url.pathname==='/index.html')){try{return send(res,200,await readFile(path.join(ROOT,'public','index.html'),'utf8'),'text/html; charset=utf-8')}catch{return send(res,500,'UI file missing','text/plain; charset=utf-8')}}
return send(res,404,JSON.stringify({error:'not_found'}));
});
server.listen(PORT,HOST,()=>console.log(JSON.stringify({event:'server_started',host:HOST,port:PORT,pid:process.pid,localOnly:HOST==='127.0.0.1'||HOST==='localhost'})));
for(const sig of ['SIGINT','SIGTERM'])process.on(sig,()=>server.close(()=>process.exit(0)));
