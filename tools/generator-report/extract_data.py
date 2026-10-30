import re, json, math, argparse, subprocess, hashlib
from datetime import datetime, timezone
from pathlib import Path
from collections import Counter
from functools import lru_cache

parser=argparse.ArgumentParser(description='从GTOCore源码提取发电与燃料数据')
parser.add_argument('--repo',type=Path,required=True)
parser.add_argument('--gtm',type=Path,required=True)
parser.add_argument('--output',type=Path,required=True)
args_cli=parser.parse_args()
ROOT=args_cli.repo.resolve()
GTM=args_cli.gtm.resolve()
OUT=args_cli.output.resolve()
OUT.parent.mkdir(parents=True,exist_ok=True)
for needed in [ROOT/'src/main/java/com/gtocore/data/recipe/FuelRecipe.java',GTM/'src/main/java/com/gregtechceu/gtceu/api/GTValues.java']:
    if not needed.is_file(): raise SystemExit('缺少源码文件：'+str(needed))
inputs_to_watch=list((ROOT/'src/main/java/com/gtocore/data').rglob('*.java'))
initial_hashes={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in inputs_to_watch}

TIERS = ['ULV','LV','MV','HV','EV','IV','LuV','ZPM','UV','UHV','UEV','UIV','UXV','OpV','MAX']
V = {t:8*4**i for i,t in enumerate(TIERS)}
VA_VALUES=[7,30,120,480,1920,7680,30720,122880,491520,1966080,7864320,31457280,125829120,503316480,2013265920]

def expr(s):
    s=s.strip().replace('GTValues.','').replace('_','')
    s=re.sub(r'\b(VA|V)\[([A-Za-z]+)\]', lambda m:str(V[m[2]] if m[1]=='V' else VA_VALUES[TIERS.index(m[2])]) if m[2] in TIERS else m[0],s)
    s=re.sub(r'(?<=\d)[LlFfDd]\b','',s)
    if not re.fullmatch(r'[\d\s+*/().<>&|\-]+',s): return None
    try: return eval(s,{'__builtins__':{}},{})
    except Exception: return None

def args(s):
    result=[]; start=depth=0; quoted=False; esc=False
    for i,c in enumerate(s):
        if quoted:
            if c=='"' and not esc: quoted=False
            esc=(c=='\\' and not esc)
        elif c=='"': quoted=True
        elif c in '([{': depth+=1
        elif c in ')]}': depth-=1
        elif c==',' and depth==0: result.append(s[start:i].strip()); start=i+1
    result.append(s[start:].strip())
    return result

def calls(s):
    found=[]
    for m in re.finditer(r'\.(\w+)\(',s):
        i=m.end(); start=i; depth=1; quoted=False; esc=False
        while i<len(s) and depth:
            c=s[i]
            if quoted:
                if c=='"' and not esc: quoted=False
                esc=(c=='\\' and not esc)
            elif c=='"': quoted=True
            elif c=='(': depth+=1
            elif c==')': depth-=1
            i+=1
        found.append((m[1],s[start:i-1]))
    return found

def snake(s):
    if s.isupper(): return s.lower()
    return re.sub(r'(?<!^)(?=[A-Z])','_',s).lower()

lang={}
for p in [ROOT/'src/generated/resources/assets/gtocore/lang/zh_cn.json',GTM/'src/main/resources/assets/gtceu/lang/zh_cn.json']:
    if p.exists(): lang.update(json.loads(p.read_text(encoding='utf-8-sig')))

@lru_cache(maxsize=None)
def label(key):
    plasma=key.endswith('@plasma'); k=key.removesuffix('@plasma').removesuffix('@liquid')
    prefix=''
    if '/' in k: prefix,k=k.split('/',1)
    if ':' in k: ns,k=k.split(':',1)
    n=next((lang[x] for x in ['material.'+k,'item.gtocore.'+k,'item.gtceu.'+k,'block.gtocore.'+k,'block.gtceu.'+k] if x in lang),None)
    if n is None:
        n=next((v for x,v in lang.items() if x.startswith(('material.','item.gtocore.')) and x.split('.')[-1].replace('_','')==k.replace('_','')),None)
    return (n or k)+(('（'+prefix+'）') if prefix else '')+('（等离子体）' if plasma else '')

def ingredient(method, raw):
    a=args(raw); kind='fluid' if 'Fluid' in method else 'item'; chance=1; consumed=not method.startswith('notConsumable')
    if method.startswith('chanced'):
        chance=(expr(a[-2]) or 0)/10000; a=a[:-2]
    quantity=expr(a[-1]) if len(a)>1 else None
    if quantity is not None: a=a[:-1]
    else: quantity=1
    key=''; prefix=''
    if kind=='fluid':
        text=','.join(a)
        if 'FluidStack(' in text:
            st=args(text[text.find('FluidStack(')+11:].rstrip(')'))
            if len(st)>1 and expr(st[-1]) is not None: quantity=expr(st[-1])
        mat=re.search(r'(?:GTOMaterials\.|GTMaterials\.)?(\w+)\.getFluid\((.*?)\)',text)
        if mat:
            key=snake(mat[1]); nested=args(mat[2])
            if nested and expr(nested[-1]) is not None: quantity=expr(nested[-1])
        else:
            literal=re.search(r'"([\w:./-]+)"',text)
            if literal: key=literal[1]
            elif 'ModFluids.CRYO_FUEL' in text: key='ad_astra:cryo_fuel'
            elif 'FluidStack(' in text:
                st=args(text[text.find('FluidStack(')+11:].rstrip(')'))
                key=snake(st[0].split('.')[-1]); quantity=expr(st[-1])
            else: key=snake(a[0].split('.')[-1])
        if 'PLASMA' in raw: key+='@plasma'
        elif 'FluidStorageKeys.LIQUID' in raw: key+='@liquid'
        elif 'ENERGY_STORAGE' in raw: key+='@charged'
        elif 'ENERGY_RELEASE' in raw: key+='@spent'
    else:
        if len(a)>1 and (a[0].startswith(('TagPrefix.','GTOTagPrefix.')) or a[0] in ['dust','dustTiny','dustSmall','gem','ingot','plate','rod']):
            prefix=a[0].split('.')[-1]; key=snake(a[1].split('.')[-1])
        else:
            text=a[0]
            lit=re.search(r'"([\w:./-]+)"',text)
            if lit: key=lit[1]
            else:
                text=re.sub(r'\.(?:asItem|get|asStack)\(.*?\)','',text)
                key=snake(text.split('.')[-1])
        if prefix:key=prefix+'/'+key
    return dict(key=key,label=label(key),qty=quantity,kind=kind,chance=chance,consumed=consumed,raw=raw)

allrecipes=[]
for p in (ROOT/'src/main/java/com/gtocore/data').rglob('*.java'):
    text=p.read_text(encoding='utf-8-sig')
    # Keep offsets while ignoring comments.
    text=re.sub(r'/\*.*?\*/|//[^\n]*',lambda m:re.sub(r'[^\n]',' ',m[0]),text,flags=re.S)
    for m in re.finditer(r'\b([A-Z][A-Z_0-9]+)\.(?:recipeBuilder|builder)\(',text):
        stop=text.find('.save();',m.end())
        if stop<0:continue
        block=text[m.start():stop]
        if len(block)>25000:continue
        c=calls(block); ident=c[0][1].strip('"'); inputs=[]; outputs=[]; eut=duration=None
        for name,raw in c[1:]:
            if name in ['inputFluids','inputItems','chancedInput','chancedInputFluids','notConsumable','notConsumableFluid']:
                inputs.append(ingredient(name,raw))
            elif name in ['outputFluids','outputItems','chancedOutput','chancedOutputFluids']:
                outputs.append(ingredient(name,raw))
            elif name=='EUt':eut=expr(raw)
            elif name=='duration':duration=expr(raw)
        allrecipes.append(dict(type=m[1],id=ident,inputs=inputs,outputs=outputs,eut=eut,duration=duration,source=str(p.relative_to(ROOT)).replace('\\','/'),line=text.count('\n',0,m.start())+1,expert=p.name=='ExpertFluidProcessing.java',raw=block))

fueltypes=['COMBUSTION_GENERATOR_FUELS','STEAM_TURBINE_FUELS','SUPERCRITICAL_STEAM_TURBINE_FUELS','GAS_TURBINE_FUELS','PLASMA_GENERATOR_FUELS','SEMI_FLUID_GENERATOR_FUELS','ROCKET_ENGINE_FUELS','NAQUADAH_REACTOR','LARGE_NAQUADAH_REACTOR_RECIPES','HYPER_REACTOR_RECIPES','ADVANCED_HYPER_REACTOR_RECIPES','ANNIHILATE_GENERATOR_RECIPES']
fuels=[r for r in allrecipes if r['type'] in fueltypes and r['eut'] is not None and r['eut']<0]
for r in fuels:
    r['eut']=abs(r['eut']);r['energy']=r['eut']*r['duration']; r['uid']=r['type']+'/'+r['id']

for r in allrecipes:
    if r['type']=='STEAM_BOILER_RECIPES' and r['duration'] and r['inputs']:
        f=dict(r);f.update(type='THERMAL_GENERATOR_FUELS',duration=math.isqrt(int(r['duration'])),eut=8,uid='THERMAL_GENERATOR_FUELS/'+r['id']);f['energy']=f['duration']*8;fuels.append(f)

keys={r['inputs'][0]['key'] for r in fuels if r['inputs']}
keys.update(['biomedium_raw','essence_medium_raw','dragon_soul_medium_raw','wyvern_infusion_solvent','draconic_infusion_solvent','soul_infusion_solvent'])
production=[]
for r in allrecipes:
    if r['eut'] is not None and r['eut']<0: continue
    for o in r['outputs']:
        if o['key'] in keys and o['qty'] and o['chance']>0 and r['inputs']:
            # Exclude symbolic template loops, but retain explicit probabilistic routes.
            if not any(x['qty'] is None for x in r['inputs']):
                production.append(dict(recipe=r,output=o))

for watched,oldhash in initial_hashes.items():
    if hashlib.sha256(watched.read_bytes()).hexdigest()!=oldhash:
        raise SystemExit('提取期间源码发生变化，请重新运行：'+str(watched))
for fuel in fuels:
    if not fuel['inputs'] or fuel['duration'] is None or fuel['duration']<=0 or fuel['inputs'][0]['qty']<=0:
        raise SystemExit('燃料配方无法完整解析：'+fuel['uid'])
def revision(repo):
    result=subprocess.run(['git','-c','safe.directory='+str(repo),'rev-parse','HEAD'],cwd=repo,capture_output=True,text=True)
    return result.stdout.strip() if result.returncode==0 else '未知（当前工作区）'
metadata=dict(generatedAt=datetime.now(timezone.utc).isoformat(),gtocoreRevision=revision(ROOT),gtmRevision=revision(GTM))
data=dict(fuels=fuels,production=production,allrecipes=allrecipes,voltage=V,tiers=TIERS,lang=lang,metadata=metadata)
OUT.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'fuel_counts':Counter(r['type'] for r in fuels),'production_routes':len(production),'recipes':len(allrecipes),'fuel_keys':sorted(keys)},ensure_ascii=False))
for r in fuels:
    if not r['inputs'] or r['duration'] is None:print('INVALID',r['id'])
