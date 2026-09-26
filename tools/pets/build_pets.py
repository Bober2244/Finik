"""Build the original Finik companions with Blender 5.2, without UI state.

Usage: Blender --background --factory-startup --python tools/pets/build_pets.py
Geometry is authored here, not downloaded. Metric Blender Z-up / front -Y.
Export is glTF Y-up / front +Z. The feet are at the origin.
"""
import bpy
import math
import json
import shutil
import os
import sys
from pathlib import Path
from mathutils import Vector, Quaternion, Matrix

sys.path.insert(0, str(Path(__file__).resolve().parent))
from polygon_body import create_body
from polygon_details import build_details
from polygon_items import build_items

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(os.environ.get('FINIK_PET_OUT', '/Users/bi_ba/Blender/Finik_models'))
ASSETS = Path(os.environ.get('FINIK_PET_ASSETS', ROOT / 'core/pet/src/main/assets/models/pets'))
DRAWABLE = Path(os.environ.get('FINIK_PET_DRAWABLE', ROOT / 'core/pet/src/main/res/drawable-nodpi'))
FPS = 30
CLIPS = {'Idle': 3.0, 'Eat': 3.2, 'Drink': 3.6, 'Happy': 2.4,
         'Sad': 3.0, 'Play': 3.6, 'Wave': 2.4}
for p in (OUT, ASSETS, DRAWABLE):
    p.mkdir(parents=True, exist_ok=True)


def mat(name, color, roughness=.68, metallic=0):
    m = bpy.data.materials.new(name)
    m.diffuse_color = (*color, 1)
    m.use_nodes = True
    b = m.node_tree.nodes.get('Principled BSDF')
    b.inputs['Base Color'].default_value = (*color, 1)
    b.inputs['Roughness'].default_value = roughness
    b.inputs['Metallic'].default_value = metallic
    return m


class PetBuilder:
    def __init__(self, kind):
        self.kind = kind
        self.parts = {}
        self.rig = None
        self.rest = {}
        colors = {
            'cat': ((.76, .42, .22), (.99, .85, .65), (.12, .50, .37)),
            'owl': ((.36, .43, .62), (.88, .86, .76), (.88, .49, .09)),
            'dog': ((.56, .32, .19), (.96, .82, .60), (.20, .48, .64)),
        }
        fur, secondary, iris = colors[kind]
        self.materials = {
            'FurPrimary': mat('FurPrimary', fur),
            'FurSecondary': mat('FurSecondary', secondary),
            'EyeIris': mat('EyeIris', iris, .27),
            'Accessory': mat('Accessory', (.10, .49, .44)),
            'EyeWhite': mat('EyeWhite', (.99, .98, .91), .3),
            'FaceDark': mat('FaceDark', (.065, .052, .063), .42),
            'NosePink': mat('NosePink', (.82, .38, .37)),
            'WarmGold': mat('WarmGold', (.99, .62, .14), .45),
            'Food': mat('Food', (.83, .24, .14), .6),
            'WaterBottle': mat('WaterBottle', (.23, .68, .85), .33),
        }

    def assign(self, obj, material, bone, group='PetBody'):
        obj.data.materials.append(self.materials[material])
        # Blender armature weights are baked into glTF JOINTS_0 / WEIGHTS_0.
        vg = obj.vertex_groups.new(name=bone)
        vg.add(list(range(len(obj.data.vertices))), 1.0, 'REPLACE')
        self.parts.setdefault(group, []).append(obj)
        for poly in obj.data.polygons:
            poly.use_smooth = True
        return obj

    def make_rig(self):
        arm = bpy.data.armatures.new('FinikCompanionSkeleton')
        self.rig = bpy.data.objects.new('FinikRig', arm)
        bpy.context.collection.objects.link(self.rig)
        bpy.context.view_layer.objects.active = self.rig
        self.rig.select_set(True)
        bpy.ops.object.mode_set(mode='EDIT')
        bones = [
            ('Root', (0,0,0), (0,0,.35), None),
            ('Hips', (0,0,.48), (0,0,.92), 'Root'),
            ('Spine', (0,0,.92), (0,0,1.25), 'Hips'),
            ('Chest', (0,0,1.25), (0,0,1.56), 'Spine'),
            ('Head', (0,0,1.56), (0,0,2.2), 'Chest'),
            ('Jaw', (0,-.25,1.91), (0,-.53,1.9), 'Head'),
            ('Tail', (0,.22,.7), (0,.72,.85), 'Hips'),
            ('Ear.L', (.55,0,2.43) if self.kind == 'dog' else (.4,0,2.35),
             (.80,-.06,1.88) if self.kind == 'dog' else (.5,0,2.75), 'Head'),
            ('Ear.R', (-.55,0,2.43) if self.kind == 'dog' else (-.4,0,2.35),
             (-.80,-.06,1.88) if self.kind == 'dog' else (-.5,0,2.75), 'Head'),
            ('Brow.L', (.265,-.38,2.43), (.265,-.38,2.55), 'Head'),
            ('Brow.R', (-.265,-.38,2.43), (-.265,-.38,2.55), 'Head'),
        ]
        for side, s in [('L',1),('R',-1)]:
            bones += [
                (f'Thigh.{side}', (s*.23,0,.64), (s*.24,0,.34), 'Hips'),
                (f'Shin.{side}', (s*.24,0,.34), (s*.24,-.03,.15), f'Thigh.{side}'),
                (f'Foot.{side}', (s*.24,-.03,.15), (s*.24,-.24,.1), f'Shin.{side}'),
                (f'UpperArm.{side}', (s*.43,0,1.42), (s*.65,-.01,1.13), 'Chest'),
                (f'Forearm.{side}', (s*.65,-.01,1.13), (s*.67,-.075,.88), f'UpperArm.{side}'),
                (f'Hand.{side}', (s*.67,-.075,.88), (s*.68,-.13,.75), f'Forearm.{side}'),
            ]
        bones += [
            ('FoodSocket', (-.67,-.075,.88), (-.67,-.075,1.03), 'Hand.R'),
            ('PropFood', (-.67,-.075,.88), (-.67,-.075,1.03), 'FoodSocket'),
            ('PropBottle', (-.67,-.075,.88), (-.67,-.075,1.03), 'Hand.R'),
            ('PropToy', (.67,-.075,.88), (.67,-.075,1.03), 'Hand.L'),
        ]
        for name, head, tail, parent in bones:
            b = arm.edit_bones.new(name)
            b.head, b.tail = head, tail
            if parent:
                b.parent = arm.edit_bones[parent]
            self.rest[name] = (Vector(head), Vector(tail), parent)
        bpy.ops.object.mode_set(mode='OBJECT')
        self.rig.show_in_front = True
        for pb in self.rig.pose.bones:
            pb.rotation_mode = 'QUATERNION'
        self.rig['asset_contract'] = 'Finik companion v1; front +Z in glTF; feet pivot'
        self.rig['food_socket'] = 'FoodSocket (right hand); PropFood may be replaced'
        self.rig['default_accessory'] = 'scarf'

    def make_geometry(self):
        # The animal has one continuous, closed polygon skin with joint loops.
        # Facial features are authored contour meshes; wearables and props are
        # purpose-shaped polygon surfaces, never stock Blender primitives.
        body = create_body(
            self.kind, rig=self.rig,
            primary_material=self.materials['FurPrimary'],
            secondary_material=self.materials['FurSecondary'],
            name='PolygonBody', add_armature_modifier=False,
        )
        self.parts['PolygonBody'] = [body]
        build_details(self.kind, materials=self.materials, rig=self.rig,
                      assign=self.assign)
        build_items(self.assign)

    def join_skin(self):
        for name, objects in self.parts.items():
            bpy.ops.object.select_all(action='DESELECT')
            for o in objects:
                o.select_set(True)
            bpy.context.view_layer.objects.active = objects[0]
            if len(objects) > 1:
                bpy.ops.object.join()
            mesh = bpy.context.object
            mesh.name=name
            bpy.ops.object.transform_apply(location=True, rotation=True, scale=True)
            mod=mesh.modifiers.new('Finik weighted skeleton','ARMATURE')
            mod.object=self.rig
            # Scene-root skin meshes avoid glTF NODE_SKINNED_MESH_NON_ROOT.
            # Their armature modifiers already establish skinning dependencies.
            for uv_layer in list(mesh.data.uv_layers):
                mesh.data.uv_layers.remove(uv_layer)
            if name.startswith('Accessory_'):
                mesh['accessory_id']=name.split('_')[1].lower()
                mesh['follows_bone']='Head' if name.endswith('Cap') else 'Chest'
            mesh['mobile_asset']=True
            self.parts[name]=[mesh]

    def reset_pose(self):
        for pb in self.rig.pose.bones:
            pb.location=(0,0,0)
            pb.rotation_quaternion=Quaternion()
            pb.scale=(1,1,1)
        for n in ['PropFood','PropBottle','PropToy']:
            self.rig.pose.bones[n].scale=(.001,.001,.001)

    def rotate(self,name,xyz):
        from mathutils import Euler
        self.rig.pose.bones[name].rotation_quaternion=Euler(xyz,'XYZ').to_quaternion()

    def arm(self,side,target,pole,hand_twist=0,hand_roll=0):
        # Two-bone analytic IK with keyed rotations, no runtime IK dependency.
        # Target/pole are in world space; all exported keyframes are skeletal.
        bpy.context.view_layer.update()
        upper=self.rig.pose.bones['UpperArm.'+side]
        lower=self.rig.pose.bones['Forearm.'+side]
        hand=self.rig.pose.bones['Hand.'+side]
        shoulder=upper.head.copy()
        target=Vector(target)
        delta=target-shoulder
        length_a=upper.length
        length_b=lower.length
        d=min(delta.length,length_a+length_b-.003)
        d=max(d,abs(length_a-length_b)+.003)
        forward=delta.normalized()
        target=shoulder+forward*d
        pole_vec=Vector(pole)-shoulder
        bend=(pole_vec-forward*pole_vec.dot(forward)).normalized()
        along=(length_a*length_a-length_b*length_b+d*d)/(2*d)
        lift=math.sqrt(max(.0001,length_a*length_a-along*along))
        elbow=shoulder+forward*along+bend*lift
        for pb,start,end in [(upper,shoulder,elbow),(lower,elbow,target)]:
            desired=(end-start).to_track_quat('Y','Z')
            pb.matrix=Matrix.Translation(start)@desired.to_matrix().to_4x4()
            bpy.context.view_layer.update()
        # Keep a nearly upright hand and the held object's vertical axis.
        # Hand rest frame maps Y downward; convert world orientation to basis.
        q=Quaternion((0,1,0),hand_roll) @ Quaternion((1,0,0),hand_twist) @ hand.bone.matrix_local.to_quaternion()
        hand.matrix=Matrix.Translation(target)@q.to_matrix().to_4x4()
        bpy.context.view_layer.update()

    def animate(self):
        import math
        rig=self.rig
        rig.animation_data_create()
        for clip,seconds in CLIPS.items():
            action=bpy.data.actions.new(clip)
            rig.animation_data.action=action
            action.use_fake_user=True
            end=round(seconds*FPS)
            # Dense samples export deterministic glTF curves and preserve prop
            # visibility when cross-fading from arbitrary preceding actions.
            for frame in range(0,end+1,3):
                self.reset_pose()
                t=frame/FPS
                u=frame/end
                w=math.sin(math.pi*u)**2
                cycle=math.sin(2*math.pi*u)
                self.rotate('Head',(.025*math.sin(2*math.pi*u),0,.02*cycle))
                self.rotate('Tail',(0,.13*math.sin(4*math.pi*u),0))
                self.rotate('Ear.L',(.025*cycle,0,.012*cycle))
                self.rotate('Ear.R',(-.025*cycle,0,.012*cycle))
                if clip=='Idle':
                    rig.pose.bones['Chest'].scale=(1+.009*cycle,1+.01*cycle,1+.009*cycle)
                    self.rotate('UpperArm.L',(.025*cycle,0,.025*cycle))
                    self.rotate('UpperArm.R',(-.025*cycle,0,-.025*cycle))
                    self.rotate('Head',(.025*cycle,0,.022*math.sin(2*math.pi*u)))
                elif clip=='Eat':
                    reach=min(1,max(0,(u-.06)/.2)) * min(1,max(0,(.94-u)/.2))
                    resting=Vector((-.57,-.22,1.20))
                    mouth=Vector((-.16,-.48,1.73))
                    self.arm('R',resting.lerp(mouth,reach),(-.90,-.28,1.45))
                    self.arm('L',(.40,-.20,1.06),(.72,-.18,1.3))
                    rig.pose.bones['PropFood'].scale=(1,1,1)
                    self.rotate('Jaw',(.13*abs(math.sin(t*9))*reach,0,0))
                    self.rotate('Head',(.04*reach,0,-.04*reach))
                    # A single bite shrinks slightly, reset in every new clip.
                    bite=1-.25*min(1,max(0,(u-.48)/.10))
                    rig.pose.bones['PropFood'].scale=(bite,)*3
                elif clip=='Drink':
                    reach=min(1,max(0,(u-.05)/.25))*min(1,max(0,(.96-u)/.25))
                    self.arm('R',Vector((-.55,-.22,1.17)).lerp(Vector((-.13,-.43,1.56)),reach),(-.90,-.33,1.3),hand_twist=.35*reach,hand_roll=.35*reach)
                    self.arm('L',(.42,-.23,1.08),(.72,-.24,1.20))
                    rig.pose.bones['PropBottle'].scale=(1,1,1)
                    self.rotate('Head',(-.07*reach,0,0))
                    self.rotate('Jaw',(.03*reach*abs(math.sin(t*8)),0,0))
                elif clip=='Happy':
                    jump=max(0,math.sin(u*4*math.pi))**2*.13*w
                    rig.pose.bones['Root'].location.z=jump
                    self.rotate('Spine',(0,.08*cycle,.07*cycle))
                    self.rotate('Head',(-.05*w,0,-.10*cycle))
                    self.arm('L',(.61,-.02,1.42+.34*w),(.9,-.15,1.25))
                    self.arm('R',(-.61,-.02,1.42+.34*w),(-.9,-.15,1.25))
                    self.rotate('Tail',(0,.40*math.sin(t*13),0))
                    self.rotate('Jaw',(.075*w,0,0))
                    for side in ('L', 'R'):
                        self.rotate('Thigh.'+side,(.24*w,0,0))
                        self.rotate('Shin.'+side,(-.37*w,0,0))
                    self.rotate('Brow.L',(0,0,.09*w))
                    self.rotate('Brow.R',(0,0,-.09*w))
                elif clip=='Sad':
                    self.rotate('Chest',(.10*w,0,0))
                    self.rotate('Head',(.17*w,0,.07*w))
                    self.rotate('Ear.L',(.10*w,0,-.15*w))
                    self.rotate('Ear.R',(.10*w,0,.15*w))
                    self.arm('L',(.40,-.23,.94),(.79,-.10,1.14))
                    self.arm('R',(-.40,-.23,.94),(-.79,-.10,1.14))
                    self.rotate('Tail',(-.15*w,0,0))
                    self.rotate('Brow.L',(0,0,-.23*w))
                    self.rotate('Brow.R',(0,0,.23*w))
                elif clip=='Play':
                    sway=.1*math.sin(t*5)*w
                    self.rotate('Head',(.08*w,0,sway))
                    self.rotate('Spine',(0,sway*.6,sway*.6))
                    self.arm('L',(.38+.08*math.sin(t*5)*w,-.40,1.33+.12*math.cos(t*5)*w),(.8,-.3,1.10))
                    self.arm('R',(-.35+.06*math.sin(t*5)*w,-.38,1.28),(-.8,-.3,1.1))
                    rig.pose.bones['PropToy'].scale=(1,1,1)
                    for side, phase in (('L', 0), ('R', math.pi)):
                        step=max(0,math.sin(t*5+phase))*.20*w
                        self.rotate('Thigh.'+side,(step,0,0))
                        self.rotate('Shin.'+side,(-step*1.5,0,0))
                    self.rotate('Tail',(0,.27*math.sin(t*9),0))
                elif clip=='Wave':
                    raise_arm=min(1,u/.23)*min(1,(1-u)/.23)
                    wave=math.sin(t*12)*.10*raise_arm
                    self.arm('L',Vector((.56,-.10,1.0)).lerp(Vector((.67+wave,-.10,2.13)),raise_arm),(.9,-.16,1.4))
                    self.rotate('Head',(0,0,-.07*raise_arm))
                    self.rotate('Hand.L',(0,0,wave*2))
                for pb in rig.pose.bones:
                    pb.keyframe_insert(data_path='location',frame=frame,group=pb.name)
                    pb.keyframe_insert(data_path='rotation_quaternion',frame=frame,group=pb.name)
                    pb.keyframe_insert(data_path='scale',frame=frame,group=pb.name)
            action['duration_seconds']=seconds
            action['loop']=clip=='Idle'
        rig.animation_data.action=bpy.data.actions['Idle']
        bpy.context.scene.frame_set(0)
        bpy.context.scene.frame_start=0
        bpy.context.scene.frame_end=90


def camera_setup():
    scene=bpy.context.scene
    scene.render.engine='BLENDER_EEVEE'
    scene.render.resolution_x=768
    scene.render.resolution_y=768
    scene.render.resolution_percentage=100
    scene.render.image_settings.file_format='PNG'
    scene.render.film_transparent=True
    scene.world.color=(.22,.22,.22)
    world=scene.world
    world.use_nodes=True
    world.node_tree.nodes['Background'].inputs['Color'].default_value=(.55,.65,.72,1)
    world.node_tree.nodes['Background'].inputs['Strength'].default_value=.35
    scene.view_settings.view_transform='AgX'
    scene.view_settings.look='AgX - Medium High Contrast'
    def point(o,at):
        o.rotation_euler=(Vector(at)-o.location).to_track_quat('-Z','Y').to_euler()
    bpy.ops.object.camera_add(location=(3.6,-8.0,3.0))
    cam=bpy.context.object
    cam.name='PreviewCamera'
    cam.data.type='ORTHO'
    cam.data.ortho_scale=3.37
    point(cam,(0,0,1.35))
    scene.camera=cam
    for name,pos,power,size,color in [
        ('Key',(-3,-4,6),480,4,(1,.89,.75)),
        ('Fill',(4,-2,3),320,3,(.74,.87,1)),
        ('Rim',(0,4,4.5),650,3,(1,.88,.7)),
    ]:
        bpy.ops.object.light_add(type='AREA',location=pos)
        light=bpy.context.object
        light.name=name
        light.data.energy=power
        light.data.shape='DISK'
        light.data.size=size
        light.data.color=color
        point(light,(0,0,1.2))


def make_pet(kind):
    bpy.ops.wm.read_factory_settings(use_empty=True)
    bpy.context.preferences.filepaths.save_version=0
    scene=bpy.context.scene
    scene.world=bpy.data.worlds.new('StudioWorld')
    scene.render.fps=FPS
    pet=PetBuilder(kind)
    pet.make_rig()
    pet.make_geometry()
    pet.join_skin()
    pet.animate()
    camera_setup()
    # The default authored preview uses a scarf. Runtime accessory switching
    # toggles exported mesh nodes; all options are included in the GLB.
    for n in ['Accessory_Bow','Accessory_Cap']:
        bpy.data.objects[n].hide_render=True
        bpy.data.objects[n].hide_set(True)
    bpy.ops.object.select_all(action='DESELECT')
    pet.rig.select_set(True)
    bpy.context.view_layer.objects.active=pet.rig
    scene.render.filepath=str(OUT/f'{kind}_preview.png')
    bpy.ops.render.render(write_still=True)
    shutil.copy2(OUT/f'{kind}_preview.png',DRAWABLE/f'pet_{kind}.png')
    # Contact sheet poses are kept as inspectable supporting artifacts.
    for clip,frame in [('Eat',45),('Drink',54),('Happy',30),('Sad',45),('Play',54),('Wave',36)]:
        pet.rig.animation_data.action=bpy.data.actions[clip]
        scene.frame_set(frame)
        scene.render.resolution_percentage=50
        scene.render.filepath=str(OUT/f'{kind}_{clip.lower()}.png')
        bpy.ops.render.render(write_still=True)
    pet.rig.animation_data.action=bpy.data.actions['Idle']
    scene.frame_set(0)
    scene.render.resolution_percentage=100
    # Keep the saved .blend relocatable between a QA build and delivery.
    scene.render.filepath=f'//{kind}_preview.png'
    for screen in bpy.data.screens:
        for area in screen.areas:
            if area.type=='VIEW_3D':
                area.spaces.active.region_3d.view_location=(0,0,1.35)
                area.spaces.active.region_3d.view_distance=4.1
                area.spaces.active.region_3d.view_rotation=scene.camera.rotation_euler.to_quaternion()
                area.spaces.active.shading.color_type='MATERIAL'
                area.spaces.active.overlay.show_floor=False
    bpy.ops.wm.save_as_mainfile(filepath=str(OUT/f'{kind}.blend'))
    for n in ['Accessory_Bow','Accessory_Cap']:
        bpy.data.objects[n].hide_render=False
        bpy.data.objects[n].hide_set(False)
    bpy.ops.object.select_all(action='DESELECT')
    pet.rig.select_set(True)
    for parts in pet.parts.values():
        parts[0].select_set(True)
    glb=OUT/f'{kind}.glb'
    bpy.ops.export_scene.gltf(
        filepath=str(glb), export_format='GLB', use_selection=True,
        export_animations=True, export_animation_mode='ACTIONS',
        export_force_sampling=True, export_frame_range=False,
        export_anim_single_armature=True, export_anim_slide_to_zero=True,
        export_skins=True, export_all_influences=False,
        export_materials='EXPORT', export_extras=True,
        export_cameras=False, export_lights=False,
        export_yup=True, export_apply=False,
        export_optimize_animation_size=True,
    )
    shutil.copy2(glb,ASSETS/glb.name)
    tris=0
    vertices=0
    for parts in pet.parts.values():
        mesh=parts[0].data
        mesh.calc_loop_triangles()
        tris+=len(mesh.loop_triangles)
        vertices+=len(mesh.vertices)
    return {
        'id':kind, 'blend':f'{kind}.blend', 'glb':f'{kind}.glb',
        'preview':f'{kind}_preview.png', 'triangles':tris,'vertices':vertices,
        'meshObjects':len(pet.parts),'bones':len(pet.rig.data.bones),
        'fileBytes':glb.stat().st_size,
    }


def main():
    manifest={
        'schemaVersion':1,
        'name':'Finik original companions',
        'license':'Original procedural artwork created for the Finik project; no external assets.',
        'coordinateSystem':{'up':'+Y','front':'+Z','origin':'center between feet at ground','unit':'meter'},
        'clips':[{'name':k,'durationSeconds':v,'loop':k=='Idle'} for k,v in CLIPS.items()],
        'customizableMaterials':['FurPrimary','FurSecondary','EyeIris','Accessory'],
        'accessories':{'scarf':'Accessory_Scarf','bow':'Accessory_Bow','cap':'Accessory_Cap','none':None},
        'defaultAccessory':'scarf',
        'propBones':{'food':'PropFood','bottle':'PropBottle','toy':'PropToy'},
        'foodSocket':'FoodSocket',
        'notes':[
            'PolygonBody is one closed connected polygon skin for the head, neck, torso, arms, and legs; see validate_mesh.py.',
            'Face, ear, tail, accessory, and prop contours are authored polygon meshes, not Blender stock primitive objects.',
            'All meshes are skinned to FinikRig; accessories follow Chest/Head bones.',
            'Every animation keys all bones and resets inactive props to scale 0.001.',
            'Eat uses a reusable bite/fruit. Attach a different food to FoodSocket and hide Prop_Food to replace it.',
            'All three accessory meshes are exported. The app selects one by node-name prefix.',
            'Use Filament material baseColorFactor for color customization; authored colors use linear RGB.',
            'Morphological growth is represented by application scale/status; models retain the same rig at each age.',
        ],
        'pets':[make_pet(k) for k in ['cat','owl','dog']],
    }
    content=json.dumps(manifest,ensure_ascii=False,indent=2)+'\n'
    (OUT/'manifest.json').write_text(content)
    (ASSETS/'manifest.json').write_text(content)
    print('FINIK_BUILD_COMPLETE '+content)


if __name__=='__main__':
    main()
