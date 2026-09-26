// Checks the actual exported binary assets, including the contract consumed by Android.
// Run `npm ci && npm run validate` from this directory.
import { readFile, mkdir, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import validator from 'gltf-validator';

const here = dirname(fileURLToPath(import.meta.url));
const root = resolve(here, '../..');
const input = process.argv[2] ? resolve(process.argv[2]) : resolve(root, 'core/pet/src/main/assets/models/pets');
const reports = resolve(here, 'reports');
await mkdir(reports, { recursive: true });
const clips = ['Idle', 'Eat', 'Drink', 'Happy', 'Sad', 'Play', 'Wave'];
const materials = ['FurPrimary', 'FurSecondary', 'EyeIris', 'Accessory'];
const accessories = ['Accessory_Scarf', 'Accessory_Bow', 'Accessory_Cap'];
let failed = false;

for (const species of ['cat', 'owl', 'dog']) {
  const errors = [];
  const bytes = await readFile(resolve(input, `${species}.glb`));
  if (bytes.toString('ascii', 0, 4) !== 'glTF' || bytes.readUInt32LE(4) !== 2) {
    throw new Error(`${species}: expected GLB 2.0`);
  }
  const jsonLength = bytes.readUInt32LE(12);
  const doc = JSON.parse(bytes.toString('utf8', 20, 20 + jsonLength));
  const result = await validator.validateBytes(new Uint8Array(bytes), {
    uri: `${species}.glb`, maxIssues: 1000,
  });
  const check = (condition, message) => { if (!condition) errors.push(message); };
  check(result.issues.numErrors === 0, `${result.issues.numErrors} Khronos validation errors`);
  check((doc.skins?.length ?? 0) > 0, 'No exported skeleton / skin');
  const names = (doc.animations ?? []).map(a => a.name);
  clips.forEach(name => check(names.includes(name), `Missing animation ${name}`));
  materials.forEach(name => check(doc.materials?.some(m => m.name === name), `Missing material ${name}`));
  accessories.forEach(prefix => {
    const nodes = (doc.nodes ?? []).filter(n => n.name?.startsWith(prefix) && n.mesh !== undefined);
    check(nodes.length > 0, `Missing accessory mesh ${prefix}`);
    nodes.forEach(n => check(n.skin !== undefined, `${n.name} is not skinned`));
  });
  for (const animation of doc.animations ?? []) {
    check(animation.channels.length > 0, `${animation.name} has no channels`);
    check(animation.samplers.some(s => (doc.accessors[s.input].max?.[0] ?? 0) > 0), `${animation.name} has no duration`);
  }
  for (const node of doc.nodes ?? []) {
    if (node.mesh === undefined || node.skin === undefined) continue;
    for (const primitive of doc.meshes[node.mesh].primitives) {
      check(primitive.attributes.JOINTS_0 !== undefined && primitive.attributes.WEIGHTS_0 !== undefined,
        `${node.name} is missing vertex skin weights`);
    }
  }
  check(!(doc.buffers ?? []).some(b => b.uri), 'GLB references an external buffer');
  check(!(doc.images ?? []).some(i => i.uri), 'GLB references an external texture');
  const summary = {
    species, bytes: bytes.length, skins: doc.skins?.length ?? 0,
    joints: doc.skins?.[0]?.joints.length ?? 0,
    meshes: doc.meshes?.length ?? 0, animations: names,
    errors, validatorErrors: result.issues.numErrors, validatorWarnings: result.issues.numWarnings,
  };
  await writeFile(resolve(reports, `${species}.json`), JSON.stringify({ summary, validator: result }, null, 2));
  console.log(JSON.stringify(summary));
  failed ||= errors.length > 0;
}
process.exitCode = failed ? 1 : 0;
