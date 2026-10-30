import fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
import { Workbook, SpreadsheetFile } from '@oai/artifact-tool';

const dataPath=process.argv[2];
const out=path.resolve(process.argv[3]);
const selectedDifficulty=Number(process.argv[4]||2);
const previewDir=process.argv[5]?path.resolve(process.argv[5]):null;
if(![1,2,3].includes(selectedDifficulty))throw new Error('难度编号必须为1、2或3');
const data=JSON.parse(await fs.readFile(dataPath,'utf8'));
const generatedAt=new Intl.DateTimeFormat('zh-CN',{timeZone:'Asia/Shanghai',dateStyle:'medium',timeStyle:'short'}).format(new Date(data.metadata.generatedAt));
const wb = Workbook.create();
const names = ['发电机总览','燃料对比','培养液','制造材料','燃料基准','计算条件'];
const sheets = Object.fromEntries(names.map(n => [n, wb.worksheets.add(n)]));
const V = data.voltage;
const categories = {
  COMBUSTION_GENERATOR_FUELS:'液体燃烧', STEAM_TURBINE_FUELS:'蒸汽', SUPERCRITICAL_STEAM_TURBINE_FUELS:'超临界蒸汽',
  GAS_TURBINE_FUELS:'燃气', PLASMA_GENERATOR_FUELS:'等离子体', SEMI_FLUID_GENERATOR_FUELS:'半流质',
  ROCKET_ENGINE_FUELS:'火箭', NAQUADAH_REACTOR:'小型硅岩', LARGE_NAQUADAH_REACTOR_RECIPES:'大型硅岩',
  HYPER_REACTOR_RECIPES:'超能',ADVANCED_HYPER_REACTOR_RECIPES:'进阶超能',ANNIHILATE_GENERATOR_RECIPES:'人造恒星',
  THERMAL_GENERATOR_FUELS:'热力',FUEL_CELL_ENERGY_RELEASE_RECIPES:'液流电池'
};
const family = {
  COMBUSTION_GENERATOR_FUELS:'内燃发电机／大型内燃机／化学能吞噬者／发电阵列',
  STEAM_TURBINE_FUELS:'单方块、大型、特大蒸汽涡轮机／发电阵列',
  SUPERCRITICAL_STEAM_TURBINE_FUELS:'大型、特大超临界蒸汽涡轮机',
  GAS_TURBINE_FUELS:'单方块、大型、特大燃气涡轮机／化学能吞噬者／发电阵列',
  PLASMA_GENERATOR_FUELS:'磁流体发电机',SEMI_FLUID_GENERATOR_FUELS:'单方块、大型半流质发电机／发电阵列',
  ROCKET_ENGINE_FUELS:'火箭引擎／大型、特大火箭引擎涡轮／化学能吞噬者／发电阵列',
  NAQUADAH_REACTOR:'单方块硅岩反应堆／发电阵列',LARGE_NAQUADAH_REACTOR_RECIPES:'大型硅岩炉',
  HYPER_REACTOR_RECIPES:'超能反应堆', ADVANCED_HYPER_REACTOR_RECIPES:'进阶超能反应堆',
  ANNIHILATE_GENERATOR_RECIPES:'人造恒星',THERMAL_GENERATOR_FUELS:'单方块热力发电机',FUEL_CELL_ENERGY_RELEASE_RECIPES:'液流燃料电池'
};
const chineseFallback = {raw_oil:'中油', 'ad_astra:cryo_fuel':'低温燃料', lava:'熔岩',
 'iron_chromium_redox_flow_battery_electrolyte':'铁铬液流电池电解液',
 'vanadium_redox_flow_battery_electrolyte':'钒液流电池电解液',
 'zinc_iodide_flow_battery_electrolyte':'碘化锌液流电池电解液',
 'organic_molecule_redox_flow_battery_electrolyte':'有机分子液流电池电解液',
 'superconducting_ion_redox_flow_battery_electrolyte':'超导离子液流电池电解液',
 'antimatter_redox_flow_battery_electrolyte':'反物质液流电池电解液'};
const normalizeKey = s => s.replaceAll('_','').toLowerCase();
const cnIndex = new Map(Object.entries(data.lang).filter(([k])=>k.startsWith('material.')||k.startsWith('item.gtocore.')||k.startsWith('item.gtceu.')).map(([k,v])=>[normalizeKey(k.split('.').at(-1)),v]));
function label(x) {
 const key=typeof x==='string'?x:x.key;
 const plain=key.replace(/@plasma|@liquid|@charged|@spent/g,'');
 const parts=plain.split('/');const material=parts.at(-1).split(':').at(-1);
 const forms={dust:'粉',dustTiny:'小撮粉',dustSmall:'小堆粉',NANITES:'纳米蜂群',ingot:'锭',plate:'板',rod:'杆',gem:'宝石',gemExquisite:'精美宝石'};
 const n=chineseFallback[plain]||cnIndex.get(normalizeKey(material))||(typeof x==='object'?x.label:material);
 const suffix=key.includes('@plasma')?'（等离子体）':key.includes('@charged')?'（储能）':key.includes('@spent')?'（释能）':key.includes('@liquid')?'（液态）':'';
 return n.replace(/（等离子体）$/, '')+(parts.length>1?'（'+(forms[parts[0]]||parts[0])+'）':'')+suffix;
}

const fmt = v => typeof v==='number'?Number(v.toPrecision(8)).toLocaleString('en-US',{maximumFractionDigits:8}):v;
const ingText = (arr, scale=1) => arr.map(x => `${label(x)} ${fmt(x.qty*scale*(x.consumed?x.chance:1))}${x.kind==='fluid'?' mB':' 个'}${!x.consumed?'（不消耗）':x.chance!==1?'（期望值）':''}`).join('、');
const ref = (r) => `${r.source}:${r.line}`;
const C=(c)=>`'计算条件'!$B$${c}`;
function col(n){let s='';do{s=String.fromCharCode(65+n%26)+s;n=Math.floor(n/26)-1;}while(n>=0);return s;}
function title(sh,text,sub,last='K'){
 sh.showGridLines=false;
 sh.getRange(`A1:${last}4`).format.font={name:'Arial',size:11,color:'#162536'};
 sh.getRange('A2').values=[[text]];
 sh.getRange('A2').format.font={name:'Arial',size:16,bold:true,color:'#233C54'};
 sh.getRange('A3').values=[[sub]];
 sh.getRange(`A4:${last}4`).format.borders={bottom:{style:'thin',color:'#7A8B9B'}};
 sh.getRange('A2').format.rowHeight=28;
 sh.getRange('A3').format.rowHeight=24;
}
function table(sh,headers,rows,widths,start=6,name='Table'){
 const end=start+rows.length;const last=col(headers.length-1);
 sh.getRange(`A${start}:${last}${end}`).values=[headers,...rows];
 const area=sh.getRange(`A${start}:${last}${end}`);
 area.format.font={name:'Arial',size:11,color:'#172B3B'};
 area.format.verticalAlignment='center';area.format.rowHeight=32;
 const h=sh.getRange(`A${start}:${last}${start}`);
 h.format={fill:'#2D4963',font:{name:'Arial',size:11,bold:true,color:'#FFFFFF'},wrapText:true,horizontalAlignment:'center',verticalAlignment:'center',rowHeight:40};
 h.format.borders={insideVertical:{style:'thin',color:'#FFFFFF'}};
 for(let i=0;i<headers.length;i++){
  sh.getRange(`${col(i)}${start}:${col(i)}${end}`).format.columnWidth=widths[i]||18;
 }
 sh.tables.add(`A${start}:${last}${end}`,true,name);
 sh.freezePanes.freezeRows(start);
 return {end,start,last};
}
function formula(sh,cell,f){sh.getRange(cell).formulas=[[f]];}

// Editable comparison conditions. Values are examples of hardware, not an asserted universal maximum.
const st=sheets['计算条件'];title(st,'计算条件与说明',`提取时间：${generatedAt}（上海时间）。成本按资源消耗量/EU计。`,'E');
const controls=[
 ['难度编号',selectedDifficulty,'1=简单，2=普通，3=专家；自定义世界规则请改为实际数值。'],
 ['燃料效率修正（百分点）',0,'默认：简单+15、普通0、专家-15个百分点。'],
 ['涡轮总功率修正P（%）',100,'转子功率×转子支架功率倍率，请输入实际值。'],
 ['涡轮总燃料效率E（%）',100,'max(100, 转子效率×(100+10×支架等级差)/100)，填写机器显示的整数值。'],
 ['高速输出倍率',3,'默认：简单4、普通3、专家2.5，大型涡轮同样适用。'],
 ['多方块发电输出容量Q（EU/t）',137438953472,'示例配置为MAX电压×64A，请改为实际动力仓电压×电流的总和。'],
 ['发电超频次数k',0,'0表示按最高燃料效率比较；根据指定超频次数和Q逐行计算实际次数。'],
 ['发电超频最低时长（tick）',20,'GTOConfig.gamePlay.defaultMinOverclockDuration默认值为20。'],
 ['磁流体动力仓等级',8,'以UV=8为例；玻璃等级至少达到该等级，按激光输出计算。'],
 ['磁流体密封方块等级',13,'注册上限OpV=13；LuV=6及以下效率为1，7及以上为等级/4。'],
 ['培养液投入量（mB）',1000000000,'上限1,000,000,000mB；按参考图材料比例，将图中1MB解释为100万B。'],
 ['生物组织数量',64,'上限64个，组织类型见培养液工作表。'],
 ['生物发电能量控制方块倍率',1.5,'MK3=1.5，MK2=1.2，其余=1。'],
 ['培养液使用时间（秒）',1000,'比较用连续发电时间；营养每秒下降1/1000，组织更换与停机时间另计。'],
 ['连杆指数r',4,'磁化钐=1，充能下界合金=2，协调腾铵=3，Magmatter=4。'],
 ['Magmatter连杆耐久',86400,'每次开始20tick的发电配方消耗1点耐久。'],
 ['发电阵列倍率',1.3,'默认：简单2、普通1.3、专家1.3；燃料按相同并行数增加。'],
 ['发电阵列机器数量上限',4,'默认：简单16台、普通4台、专家4台。'],
 ['发电阵列无线损耗率',0.05,'默认：简单4%、普通5%、专家8%；接收端额外损耗另计。'],
 ['风力最大输出电流（A）',1,'默认：简单2A、普通1A、专家1A；实际发电量随风力和转子变化。'],
 ['光伏行星系数S',1,'取PlanetApi.getSolarPower(level)，请按实际行星填写。'],
 ['戴森球模块数',1,'初始建造与发射费用不计入持续燃料费用。'],
 ['戴森球损伤值',0,'输出系数=(50-max(0,损伤-60))/50。'],
];
table(st,['项目','值','含义'],controls,[40,25,105],6,'Conditions');
// rows 7..29
formula(st,'B8','=CHOOSE(B7,15,0,-15)');
formula(st,'B11','=CHOOSE(B7,4,3,2.5)');
formula(st,'B23','=CHOOSE(B7,2,1.3,1.3)');
formula(st,'B24','=CHOOSE(B7,16,4,4)');
formula(st,'B25','=CHOOSE(B7,0.04,0.05,0.08)');
formula(st,'B26','=CHOOSE(B7,2,1,1)');
st.getRange('B7:B29').format.fill='#FFF4CD';st.getRange('B7:B29').setNumberFormat('#,##0.00');
st.getRange('B25').setNumberFormat('0.0%');
st.getRange('B7').dataValidation={rule:{type:'list',values:['1','2','3']}};
for(const [cell,min,max] of [['B13',0,5],['B15',1,14],['B16',0,13],['B18',1,64],['B21',1,4]])st.getRange(cell).dataValidation={rule:{type:'whole',operator:'between',formula1:min,formula2:max}};
const notes=[
 ['单位','1B=1,000mB，1MEU=1,000,000EU，1秒=20tick；EU/t表示功率，燃料效率单独列出。'],
 ['基准与机型对比','燃料基准列出原始配方；燃料对比计入机器效率、时长向下取整、扩展、氧气及超频。'],
 ['最大输出','固定上限直接列值；依赖转子、激光或动力仓的机器，按计算条件中的配置计算上限，不视为所有配置的统一极限。'],
 ['最高燃料效率','发电每次超频功率×4、时长×0.2；达到最低时长后燃料×5。最高燃料效率与高功率运行分别计算。'],
 ['净发电量','表中EU为发电机总产出；燃料制造、电刺激、运输、计算设备耗电、维护材料及接收端损耗不自动扣除。'],
 ['制造材料范围','制造材料表仅展开最近一道配方；不抵扣副产物价值，不递归展开采矿、精炼等全部工序；替代配方不得相加。'],
 ['生物发电','培养液量与输出成正比，营养下降速度与液量无关；培养液成本按不停机且及时补充同类组织计算；刺激峰值为瞬时值。'],
 ['培养液寿命','营养从1000开始每秒下降1，低于阈值时排液；阈值为0时营养0仍可能保留1秒。受投入时刻和配方等待影响，实际约1000～1001秒。'],
 ['热力发电固体燃料','固体燃料由FurnaceBlockEntity.getFuel()与GTUtil.getItemBurnTime运行时注册。设熔炉燃烧时长为b tick，则基准时长=floor(sqrt(8b))，基准能量=8×时长；仅静态源码无法确定所有模组的完整清单。'],
 ['液流燃料电池','放电消耗200mB储能电解液，回收190mB释能电解液，净补充10mB；化学燃料用于充电侧。膜效率作用于充电侧，膜放电并行数作用于放电侧。'],
 ['实际机器条件','需要足量燃料及助剂、转子满速、排气与输出容量；超过Q的行不满足当前输出条件。是否发电以实现为准，不仅看机器名称。'],
 ['材料名','材料、机器和组织优先采用仓库中文名称；配方ID、源码路径及等级缩写保留原标识。'],
 ['源码版本',`GTOCore ${data.metadata.gtocoreRevision}；GTM fork ${data.metadata.gtmRevision}；按当前工作区提取，包含未提交更改。`],
 ['验证方法','已将公式与源码定义独立核对，未启动游戏或运行Gradle；运行时数据包及自定义世界规则需按实际补充。'],
];
table(st,['类别','','说明'],notes.map(r=>[r[0],'',r[1]]),[40,25,105],33,'MethodNotes');st.getRange('C34:C47').format.wrapText=true;st.getRange('A34:C47').format.rowHeight=72;
table(st,['参考项','','出处'],[
 ['机器效率','GTOLib/src/main/java/com/gtolib/utils/GTOUtils.java:105'],
 ['单方块发电时长取整','GTOLib/src/main/java/com/gtolib/api/recipe/GTORecipeModifiers.java:61'],
 ['发电超频','../GregTech-Modern/src/main/java/com/gregtechceu/gtceu/api/recipe/modifier/RecipeModifier.java:229'],
 ['默认规则','src/main/java/com/gtocore/config/GTORules.java:153'],
 ['热力燃料派生规则','src/main/java/com/gtocore/common/recipe/RecipeTypeModify.java:147'],
 ['培养液与组织发电公式','src/main/java/com/gtocore/common/machine/multiblock/generator/BioOscillationGenerator.java:197'],
 ['最低超频时长','src/main/java/com/gtocore/config/GTOConfig.java:288'],
].map(r=>[r[0],'',r[1]]), [40,25,105],51,'MethodSources');

// Dynamic electrolyte release definitions.
const electrolyte=[
 ['iron_chromium_redox_flow_battery_electrolyte','铁铬电解液',Math.floor(V.UEV/50)],
 ['vanadium_redox_flow_battery_electrolyte','钒电解液',Math.floor(V.UIV*3/50)],
 ['zinc_iodide_flow_battery_electrolyte','碘化锌电解液',Math.floor(V.UXV*9/50)],
 ['organic_molecule_redox_flow_battery_electrolyte','有机分子电解液',Math.floor(V.OpV*20/50)],
 ['superconducting_ion_redox_flow_battery_electrolyte','超导离子电解液',Math.floor(V.MAX*80/50)],
 ['antimatter_redox_flow_battery_electrolyte','反物质电解液',Math.floor(V.MAX*900/50)],
];
const fuels=[...data.fuels];
for(const [key,name,eut] of electrolyte)fuels.push({uid:`FUEL_CELL_ENERGY_RELEASE_RECIPES/${key}`,id:key,type:'FUEL_CELL_ENERGY_RELEASE_RECIPES',eut,duration:200,energy:eut*200,expert:false,
 inputs:[{key:key+'@charged',label:name+'（储能）',qty:200,kind:'fluid',chance:1,consumed:true}],
 outputs:[{key:key+'@spent',label:name+'（释能）',qty:190,kind:'fluid',chance:1,consumed:true}],source:'src/main/java/com/gtocore/data/recipe/classified/FuelCell.java',line:88} );
assert.ok(fuels.length>0,'未找到燃料定义');
const fuelRow=new Map();
const br=sheets['燃料基准'];title(br,'燃料配方基准值','未应用机器效率、扩展和发电超频的基准值；物品燃料以个/EU计。','O');
const rawRows=fuels.map((r,i)=>{
 const f=r.inputs[0],aux=r.inputs[1];fuelRow.set(r.uid,i+7);
 return [categories[r.type],label(f),family[r.type],f.qty,f.kind==='item'?'个':'mB',aux?label(aux):'无',aux?.qty||0,r.duration,r.eut,null,null,null,null,ingText(r.outputs),r.expert?'流体规则为专家时':'全部难度',r.id,'',ref(r)];
});
const rawT=table(br,['类别','燃料','对应发电机','每次燃料量','单位','助剂','每次助剂量','时长tick','基准EU/t','EU/次','每单位燃料EU','燃料量/EU','助剂mB/EU','每次副产物','注册条件','配方ID','','出处'],rawRows,[19,36,52,14,9,30,14,13,18,21,20,19,19,65,24,39,3,110],6,'FuelDefinitions');
for(let r=7;r<=rawT.end;r++){
 formula(br,`J${r}`,`=H${r}*I${r}`);formula(br,`K${r}`,`=J${r}/D${r}`);formula(br,`L${r}`,`=D${r}/J${r}`);formula(br,`M${r}`,`=G${r}/J${r}`);
}
br.getRange(`D7:K${rawT.end}`).setNumberFormat('#,##0');br.getRange(`K7:K${rawT.end}`).setNumberFormat('#,##0.000');br.getRange(`L7:M${rawT.end}`).setNumberFormat('0.000000E+00');
br.freezePanes.freezeColumns(2);

// Machine configurations. At k=0 the reactor rows are on the highest fuel efficiency side.
const specs=[];
function spec(name,config,type,mode,values={}){specs.push({name,config,type,mode,...values});}
for(const [name,type,tiers] of [
 ['热力发电机','THERMAL_GENERATOR_FUELS',[0,1,2]],['半流质发电机','SEMI_FLUID_GENERATOR_FUELS',[0,1,2,3]],
 ['内燃发电机','COMBUSTION_GENERATOR_FUELS',[1,2,3]],['燃气涡轮机','GAS_TURBINE_FUELS',[1,2,3]],['蒸汽涡轮机','STEAM_TURBINE_FUELS',[1,2,3]],
 ['火箭引擎','ROCKET_ENGINE_FUELS',[4,5,6]],['硅岩反应堆','NAQUADAH_REACTOR',[5,6,7]]]){
 for(const t of tiers)spec(name,data.tiers[t],type,'simple',{t,cap:8*4**t*(t>0&&t<4?2:1)});
}
for(const [name,type,t] of [['大型半流质发电机','SEMI_FLUID_GENERATOR_FUELS',4],['大型内燃机','COMBUSTION_GENERATOR_FUELS',4],['极限内燃机','COMBUSTION_GENERATOR_FUELS',5]]){
 for(const ext of [false,true])for(const boost of [false,true])spec(name,`${ext?'有扩展':'基础'}、${boost?'有氧气':'无氧气'}`,type,'engine',{t,ext,boost,cap:(8*4**t)*(ext?8:2)*(boost?2:1),gain:boost?(t>4?2:1.5):1});
}
for(const boost of [0,1,2])for(const type of ['COMBUSTION_GENERATOR_FUELS','GAS_TURBINE_FUELS','ROCKET_ENGINE_FUELS'])spec('化学能吞噬者',boost===2?'液氧＋N2O4':boost===1?'液氧':'无增幅',type,'devourer',{boost,cap:8192*2**(4+boost),gain:2**boost});
for(const [name,type,t,mega,special] of [
 ['大型蒸汽涡轮机','STEAM_TURBINE_FUELS',3,false,false],['大型燃气涡轮机','GAS_TURBINE_FUELS',4,false,false],
 ['大型火箭引擎涡轮','ROCKET_ENGINE_FUELS',4,false,true],['超临界蒸汽涡轮机','SUPERCRITICAL_STEAM_TURBINE_FUELS',5,false,false],
 ['特大蒸汽涡轮机','STEAM_TURBINE_FUELS',4,true,false],['特大燃气涡轮机','GAS_TURBINE_FUELS',5,true,false],
 ['特大火箭引擎涡轮','ROCKET_ENGINE_FUELS',5,true,true],['特大超临界蒸汽涡轮机','SUPERCRITICAL_STEAM_TURBINE_FUELS',6,true,false]]){
 for(const ext of [false,true])for(const fast of [false,true])spec(name,`${ext?'有扩展':'基础'}、${fast?'高速模式':'普通模式'}、满速时`,type,'turbine',{t,mega,special,ext,fast,base:(8*4**t)*(mega?4:1)*(special?2.5:2)});
}
spec('磁流体发电机','激光、基础','PLASMA_GENERATOR_FUELS','magnetic',{ext:false});
spec('磁流体发电机','激光、扩展','PLASMA_GENERATOR_FUELS','magnetic',{ext:true});
spec('大型硅岩炉','按发电超频设置','LARGE_NAQUADAH_REACTOR_RECIPES','oc',{parallel:1});
spec('超能反应堆','无额外等离子体','HYPER_REACTOR_RECIPES','oc',{parallel:1});
spec('超能反应堆','额外1mB等离子体可获16并行','HYPER_REACTOR_RECIPES','oc',{parallel:16,booster:'燃料I～IV分别消耗山铜、末影素、魔金、亚稳态𬭶等离子体'});
for(const p of [1,8,16])spec('进阶超能反应堆',p===1?'无额外助剂':p===8?'星辉等离子体1mB':'致密中子等离子体1mB','ADVANCED_HYPER_REACTOR_RECIPES','oc',{parallel:p,booster:p===1?null:'等离子体1mB/次'});
spec('人造恒星','配备无线接口','ANNIHILATE_GENERATOR_RECIPES','oc',{parallel:1,wireless:true});
spec('液流燃料电池','放电并行上限384的膜','FUEL_CELL_ENERGY_RELEASE_RECIPES','oc',{parallel:384});
for(const [type,tiers] of [['STEAM_TURBINE_FUELS',[1,2,3]],['COMBUSTION_GENERATOR_FUELS',[1,2,3]],['GAS_TURBINE_FUELS',[1,2,3]],['SEMI_FLUID_GENERATOR_FUELS',[0,1,2,3]],['ROCKET_ENGINE_FUELS',[4,5,6]],['NAQUADAH_REACTOR',[5,6,7]]]){
 const t=tiers.at(-1);spec('发电阵列',`${categories[type]}、${data.tiers[t]}按上限数量装入、有线`,type,'array',{t,wireless:false});
 spec('发电阵列',`${categories[type]}、${data.tiers[t]}按上限数量装入、无线`,type,'array',{t,wireless:true});
}
function etaExpr(s){
 if(['simple','array'].includes(s.mode))return `(${s.type==='STEAM_TURBINE_FUELS'?135-35*s.t:s.type==='NAQUADAH_REACTOR'?100+50*(s.t-5):s.type==='THERMAL_GENERATOR_FUELS'?100-25*s.t:105-5*s.t}+${C(8)})/100`;
 if(['engine','devourer'].includes(s.mode))return String(s.gain);
 if(s.mode==='turbine')return `${C(10)}/100*${s.ext?(s.mega?1.3:1.2):1}`;
 if(s.mode==='magnetic')return `IF(${C(16)}>6,${C(16)}/4,1)*${s.ext?2:1}`;
 return '1';
}
function capExpr(s){
 if(s.mode==='simple'||s.mode==='engine'||s.mode==='devourer')return String(s.cap);
 if(s.mode==='array')return `${8*4**s.t*(s.t>0&&s.t<4?2:1)}*${C(23)}*${C(24)}`;
 if(s.mode==='turbine')return `MIN(${C(12)},INT(${s.base}*${C(9)}/100*${s.ext?(s.mega?3:2):1}*${s.fast?C(11):1}))`;
 return C(12);
}
const cmp=sheets['燃料对比'];title(cmp,'各发电机的燃料成本与功率','同种燃料的单位EU成本随机型、等级和扩展变化；修改计算条件可重算，最高燃料效率比较取k=0。','L');
const comparison=[];const links=[];
for(const s of specs){
 const start=7+comparison.length;
 for(const fuel of fuels.filter(f=>f.type===s.type)){
  const r=7+comparison.length,b=fuelRow.get(fuel.uid);
  const isOc=['oc','magnetic'].includes(s.mode);
  const extra=s.mode==='engine'?`润滑油1mB/72t${s.boost?(s.t>4?'、液氧80mB/20t':'、氧气20mB/20t'):''}`:s.mode==='devourer'?`润滑油10mB/72t${s.boost?'、液氧320mB/20t':''}${s.boost===2?'、N2O4 480mB/20t':''}`:s.booster?s.booster:s.mode==='turbine'?'转子损耗和维护费另计':s.mode==='oc'&&s.parallel===384?'膜的放电并行为384，同时消耗膜耐久':'';
  comparison.push([s.name,s.config,label(fuel.inputs[0]),null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,extra,fuel.expert?'流体专家规则限定':'全部难度',fuel.uid]);
  formula(cmp,`D${r}`,`=${etaExpr(s)}`);
  // duration before overclocking. Combustion bonuses increase power, not duration.
  formula(cmp,`M${r}`,`=MAX(0,INT('燃料基准'!H${b}*${['engine','devourer'].includes(s.mode)?1:`D${r}`}))`);
  if(isOc){
   const requested=s.mode==='magnetic'?`${s.ext?256:64}*${s.ext?4:2}^(${C(15)}-1)`:String(s.parallel||1);
   formula(cmp,`N${r}`,`=MAX(0,MIN(${requested},INT(${s.wireless?'9007199254740991':C(12)}/'燃料基准'!I${b})))`);
   formula(cmp,`Y${r}`,`=IF(N${r}=0,0,MIN(${C(13)},MAX(0,INT(ROUND(LOG(MAX(1,${s.wireless?'9223372036854775807':C(12)}/('燃料基准'!I${b}*MAX(1,N${r}))),4),10)))))`);
   formula(cmp,`O${r}`,`=MIN(Y${r},MAX(0,INT(ROUND(LOG(MAX(1,M${r}/${C(14)}),5),10))))`);
   formula(cmp,`P${r}`,`=IF(N${r}=0,0,INT(M${r}/5^O${r}))`);
   formula(cmp,`Q${r}`,`=N${r}*5^(Y${r}-O${r})`);
   formula(cmp,`E${r}`,`='燃料基准'!I${b}*N${r}*4^Y${r}`);
   formula(cmp,`R${r}`,`=IF(Q${r}=0,0,E${r}*P${r}/Q${r})`);
  }else{
   formula(cmp,`Y${r}`,'=0');
   formula(cmp,`N${r}`,`=MAX(0,INT((${capExpr(s)})/'燃料基准'!I${b}))`);
   formula(cmp,`P${r}`,`=M${r}`);formula(cmp,`Q${r}`,`=N${r}`);
   formula(cmp,`E${r}`,`='燃料基准'!I${b}*N${r}*${['engine','devourer'].includes(s.mode)?`D${r}`:1}*${s.wireless?`(1-${C(25)})`:1}`);
   formula(cmp,`R${r}`,`=IF(N${r}=0,0,E${r}*P${r}/N${r})`);
  }
  formula(cmp,`F${r}`,`=IF(OR(R${r}=0,M${r}=0),"不可用",'燃料基准'!D${b}/R${r})`);
  formula(cmp,`G${r}`,`=IF(ISNUMBER(F${r}),F${r}*1000000,"不可用")`);
  formula(cmp,`H${r}`,`=IF(R${r}=0,"不可用",'燃料基准'!G${b}/R${r})`);
  formula(cmp,`I${r}`,`='燃料基准'!E${b}`);
  const lub=s.mode==='engine'?1/72:s.mode==='devourer'?10/72:0;
  formula(cmp,`J${r}`,`=IF(E${r}=0,"不可用",${lub}/E${r})`);
  const boost1=s.mode==='engine'&&s.boost?(s.t>4?4:1):s.mode==='devourer'&&s.boost?16:0;
  const boost2=s.mode==='devourer'&&s.boost===2?24:0;
  formula(cmp,`K${r}`,`=IF(E${r}=0,"不可用",${boost1}/E${r})`);
  formula(cmp,`L${r}`,`=IF(E${r}=0,"不可用",${boost2}/E${r})`);
  formula(cmp,`S${r}`,`=IF(R${r}=0,"不可用",R${r}/'燃料基准'!J${b})`);
  formula(cmp,`T${r}`,`=IF(AND(N${r}>0,M${r}>0,E${r}<=${s.mode==='simple'?s.cap:s.wireless?'9223372036854775807':C(12)}),"符合输出容量","需检查功率及效率条件")`);
  formula(cmp,`U${r}`,`=IF(R${r}=0,"不可用",${fuel.type==='FUEL_CELL_ENERGY_RELEASE_RECIPES'?10:fuel.inputs[0].qty}/R${r})`);
  formula(cmp,`Z${r}`,`=IF(OR(E${r}=0,P${r}=0),"不可用",${s.booster?1:0}/(E${r}*P${r}))`);
 }
 links.push({...s,start,end:6+comparison.length});
}
// Write only literals so formula cells are preserved.
const cmpHeaders=['发电机','配置','使用燃料','效率设置倍率','配置功率上限 EU/t','燃料量/EU','燃料量/MEU','配方助剂mB/EU','燃料单位','润滑油mB/EU','氧气或液氧mB/EU','N2O4 mB/EU','超频前时长tick','基础并行数','缩短时长的超频次数','运行时长tick','每次燃料倍数','每份原配方实际产能EU','实际燃料效率','输出条件','净补充量/EU','补充说明','注册条件','燃料标识','实际超频次数','并行增幅等离子体mB/EU'];
const cmpEnd=6+comparison.length;
for(let r=0;r<comparison.length;r++){
 const ar=comparison[r];for(const j of [0,1,2,21,22,23])cmp.getCell(r+6,j).values=[[ar[j]]];
}
cmp.getRange('A6:Z6').values=[cmpHeaders];
cmp.getRange(`A6:Z${cmpEnd}`).format.font={name:'Arial',size:11,color:'#172B3B'};
cmp.getRange(`A6:Z${cmpEnd}`).format.rowHeight=32;cmp.getRange(`A6:Z${cmpEnd}`).format.verticalAlignment='center';
cmp.getRange('A6:Z6').format={fill:'#2D4963',font:{name:'Arial',size:11,bold:true,color:'#FFFFFF'},wrapText:true,horizontalAlignment:'center',verticalAlignment:'center',rowHeight:48};
const cmpWidths=[29,43,36,15,22,20,20,21,20,20,20,20,17,18,18,18,20,24,18,30,24,76,24,62,18,28];
cmpWidths.forEach((v,i)=>cmp.getRange(`${col(i)}6:${col(i)}${cmpEnd}`).format.columnWidth=v);
cmp.tables.add(`A6:Z${cmpEnd}`,true,'GeneratorFuelComparison');cmp.freezePanes.freezeRows(6);cmp.freezePanes.freezeColumns(3);
cmp.getRange(`E7:E${cmpEnd}`).setNumberFormat('#,##0');cmp.getRange(`F7:L${cmpEnd}`).setNumberFormat('0.000000E+00');cmp.getRange(`D7:D${cmpEnd}`).setNumberFormat('0.00"倍"');
cmp.getRange(`G7:G${cmpEnd}`).setNumberFormat('#,##0.000000');cmp.getRange(`M7:R${cmpEnd}`).setNumberFormat('#,##0');cmp.getRange(`S7:S${cmpEnd}`).setNumberFormat('0.0%');cmp.getRange(`U7:U${cmpEnd}`).setNumberFormat('0.000000E+00');cmp.getRange(`Z7:Z${cmpEnd}`).setNumberFormat('0.000000E+00');
cmp.getRange(`T7:T${cmpEnd}`).conditionalFormats.add('containsText',{text:'確認',format:{fill:'#FCE8E6',font:{color:'#9C2824'}}});

// Front page: generator definitions and fixed / configurable limits.
const front=sheets['发电机总览'];title(front,'发电机与燃料对比','功率上限取决于配置；各燃料的单位EU成本见燃料对比，原料需求见制造材料。','I');
const overviewRows=links.map(s=>[
 s.name,s.config,categories[s.type],null,null,
 s.mode==='simple'?'燃烧时长按机器等级效率修正，向下取整。':s.mode==='engine'?'扩展使输出×4，供氧还能提高每份燃料的发电量。':s.mode==='devourer'?'两种助剂同时供应时，燃料效率为基准的4倍。':s.mode==='turbine'?'按设置的P、E和转子满速计算；高速模式增加功率和转子损耗。':s.mode==='magnetic'?'激光仓等级提高并行倍率；OpV密封方块加扩展时燃料效率最高6.5倍。':s.mode==='array'?'功率随机器数量与世界规则变化；无线行已扣除发电阵列侧损耗。':'发电超频提高功率并增加单位EU燃料成本；k=0为最高燃料效率侧。',
 `燃料对比 ${s.start}～${s.end}行`
]);
const ot=table(front,['发电机','配置','燃料类别','配置功率上限 EU/t','最高实际燃料效率','功率及效率条件','燃料对应行'],overviewRows,[31,46,23,24,22,95,37],6,'GeneratorOverview');
for(let i=0;i<links.length;i++){
 const r=i+7,s=links[i];formula(front,`D${r}`,`=MAX('燃料对比'!E${s.start}:E${s.end})`);formula(front,`E${r}`,`=MAX('燃料对比'!S${s.start}:S${s.end})`);
}
front.getRange(`D7:D${ot.end}`).setNumberFormat('#,##0');front.getRange(`E7:E${ot.end}`).setNumberFormat('0.0%');front.freezePanes.freezeColumns(1);
const otherStart=ot.end+4;
const others=[
 ['生物振荡发电机','5种培养液与5级组织','8×组织等级^r×组织数×液量×外壳倍率×刺激倍率','见培养液工作表，分别列出无刺激输出与刺激瞬时峰值。'],
 ...[1,4,16].map((rate,i)=>[['充能','脉冲','振动'][i]+'光伏电站','阳光',`15 × floor(${rate} × 行星系数 S)`,'按晴天正午的EU输出计算；夜间0，雨天×0.7，雷雨×0.3；虚空维度为14×floor(rate×S)。']),
 ...[1,4,16].map((rate,i)=>[['充能','脉冲','振动'][i]+'光伏帆板控制器','阳光＋蒸馏水',`16 × floor(${rate} × 行星系数 S)`,'仅宇宙可用，每20tick消耗floor(floor(rate×S)/4)mB蒸馏水；蒸馏水/EU=floor(basic/4)/(20×16×basic)。']),
 ...[0,1,2,3].map(t=>['风力发电机 '+data.tiers[t],'风＋动能转子',`${8*4**t} × 风力输出电流`,'此值为送电上限；实际发电=V×spinSpeed×20×A/(10+10tier)，随高度与天气变化。']),
 ...[4,5,6].map(t=>['避雷针 '+data.tiers[t],'落雷',`${256*8*4**(t-1)}`,'此值为送电上限EU/t，实际取决于落雷频率和储电量，不能作为持续发电功率。']),
 ...[1,2].map(t=>['原始魔法能源吸收器 '+data.tiers[t],'魔力＋末地水晶',String(2*8*4**t),'每秒补充相当于2V EU/t的电量，消耗0.05魔力/EU；removeMana的20为速率限制倍数，并需足够输出电流。']),
 ['戴森球接收站','已部署模块＋极寒之凛冰','V[MAX]×模块数×(50-max(0,损伤-60))/50','每20tick消耗max(1,floor(sqrt(模块数)))mB冷媒，并需满足算力和行星条件。'],
 ['裂变反应堆','Uranium／Thorium／MOX／Naquadah燃料棒','不直接输出EU','热源，产出蒸汽或高温钠钾；需接涡轮或热交换器，每份燃料发电量取决于组件布局和冷却。'],
 ['热交换器','高温钠钾、超临界钠钾、等离子体','不直接输出EU','热源，按产出的高压及超临界蒸汽接入对应涡轮计算发电成本。'],
 ['旧大型等离子体涡轮','注册已禁用','n.a.','GeneratorMultiblockRegisters:132返回占位定义；使用磁流体发电机或热交换路线。'],
 ['魔力发生器／电刺激器','消耗电力','不计为EU发电机','前者将EU转为魔力，后者用于生物振荡发电机增幅，均不独立产出EU。'],
];
table(front,['机器','燃料或能源','功率类别','生成或送电上限EU/t','燃料量/EU','计算条件','功率计算式'],others.map(r=>[r[0],r[1],r[2]==='不直接输出EU'?'热量输出':r[2]==='n.a.'?'禁用':'取决于配置',/^\d+$/.test(r[2])?Number(r[2]):'参见公式','n.a.',r[3],r[2]]),[31,46,23,24,22,95,110],otherStart,'OtherGeneration');
for(let i=1;i<=3;i++){
 const r=otherStart+1+i;formula(front,`D${r}`,`=15*INT(${[1,4,16][i-1]}*${C(27)})`);formula(front,`E${r}`,'=0');
}
for(let i=4;i<=6;i++){
 const r=otherStart+1+i;const basic=`INT(${[1,4,16][i-4]}*${C(27)})`;
 formula(front,`D${r}`,`=16*${basic}`);formula(front,`E${r}`,`=IF(D${r}=0,"n.a.",INT(${basic}/4)/(20*D${r}))`);
}
for(let i=7;i<=10;i++){const r=otherStart+1+i;formula(front,`D${r}`,`=${8*4**(i-7)}*${C(26)}`);}
for(let i=14;i<=15;i++)formula(front,`E${otherStart+1+i}`,'=0.05');
const dysonRow=otherStart+17;
formula(front,`D${dysonRow}`,`=${V.MAX}*${C(28)}*(50-MAX(0,${C(29)}-60))/50`);
formula(front,`E${dysonRow}`,`=IF(D${dysonRow}<=0,"n.a.",MAX(1,INT(SQRT(${C(28)})))/(20*D${dysonRow}))`);
front.getRange(`D${otherStart+1}:D${otherStart+others.length}`).setNumberFormat('#,##0');
front.getRange(`E${otherStart+1}:E${otherStart+others.length}`).setNumberFormat('0.000000E+00');

// Biological generation and the reference image's material quantities.
const bio=sheets['培养液'];title(bio,'培养液成本与生物发电','参考图数量按1,000,000,000mB列示；组织、连杆和刺激耗电分别注明。','K');
const tissues=[
 ['生物心肌细胞簇',1,'UV','biomedium_raw',12.1,[1.5,12.1,8.2,2.5]],
 ['突变心肌细胞簇',2,'UHV','essence_medium_raw',18,[1.8,18,17.2,1.6]],
 ['龙心肌细胞簇',3,'UEV','dragon_soul_medium_raw',20.4,[2.5,17.2,20.4,1.3]],
 ['改造龙心',4,'UIV','wyvern_infusion_solvent',22.5,[3.8,18.25,22.5,1.2]],
 ['觉醒龙心',5,'UXV','draconic_infusion_solvent',32.6,[5.1,10.25,32.6,1.2]]
];
const mediumLabels=['生物培养基原液','精华培养基原液','龙魂培养基原液','飞龙灌注溶剂','神龙灌注溶剂'];
const bioRows=tissues.map((t,i)=>[mediumLabels[i],t[2],t[0],t[1],null,null,t[4],null,null,null,null,t[5].join(' / '),t[3]]);
const bt=table(bio,['培养液','组织解锁阶段','生物组织','组织等级','无刺激输出EU/t','刺激峰值EU/t','最大刺激倍率','培养液mB/EU','培养液mB/MEU','连杆个/EU','无刺激总发电量EU','刺激倍率：幼/成/壮/老','材料ID'],bioRows,[31,16,30,15,25,25,17,23,23,22,28,43,40],6,'BioPower');
for(let i=0;i<5;i++){
 const r=7+i;formula(bio,`E${r}`,`=INT(8*D${r}^${C(21)}*${C(18)}*${C(17)}*${C(19)})`);
 formula(bio,`F${r}`,`=INT(E${r}*G${r})`);formula(bio,`K${r}`,`=E${r}*20*${C(20)}`);
 formula(bio,`H${r}`,`=${C(17)}/K${r}`);formula(bio,`I${r}`,`=H${r}*1000000`);
 formula(bio,`J${r}`,`=1/(${C(22)}*20*E${r})`);
}
bio.getRange('E7:F11').setNumberFormat('#,##0');bio.getRange('H7:J11').setNumberFormat('0.000000E+00');bio.getRange('K7:K11').setNumberFormat('0.000000E+00');
const mKeys=tissues.map(t=>t[3]);
const bioMatRows=[];
for(let i=0;i<mKeys.length;i++)for(const entry of data.production.filter(p=>p.output.key===mKeys[i])){
 for(const inp of entry.recipe.inputs){
  const r=bioMatRows.length+16;const bioR=i+7;
  bioMatRows.push([mediumLabels[i],entry.recipe.type,entry.recipe.id,label(inp),inp.kind==='fluid'?'mB':'个',inp.qty,inp.chance,entry.output.qty,null,null,null,'',ref(entry.recipe)]);
 }
}
const bmt=table(bio,['培养液','制造方式','配方ID','所需材料','单位','每次投入量','消耗概率','每次培养液产量','每10亿mB所需材料','材料量/EU','材料量/MEU','','出处'],bioMatRows,[31,38,34,46,10,18,17,22,26,23,23,3,110],15,'BioMaterials');
for(let i=0;i<bioMatRows.length;i++){
 const r=i+16,ix=mediumLabels.indexOf(bioMatRows[i][0]),p=ix+7;
 formula(bio,`I${r}`,`=F${r}*G${r}/H${r}*1000000000`);
 formula(bio,`J${r}`,`=F${r}*G${r}/H${r}*$H$${p}`);formula(bio,`K${r}`,`=J${r}*1000000`);
}
bio.getRange(`F16:I${bmt.end}`).setNumberFormat('#,##0.########');bio.getRange(`G16:G${bmt.end}`).setNumberFormat('0.0%');bio.getRange(`J16:K${bmt.end}`).setNumberFormat('0.000000E+00');
const bns=bmt.end+4;
const bioNotes=[
 ['无刺激输出','P=floor(8×组织tier^r×组织数×投入mB×外壳倍率)，默认64个组织、Magmatter连杆、MK3及10亿mB。'],
 ['适用培养液','表列培养液为该组织的最低要求；高级培养液可供低级组织使用，同种组织的输出公式不因培养液等级改变。'],
 ['刺激峰值','瞬时输出按成长阶段的最大刺激倍率计算；Java float精度可能影响末尾数位，此值不是持续运行平均功率。'],
 ['刺激耗电','组织tier1～5的刺激基准能量依次为2^23、2^25、2^27、2^29、2^31；耗电EU/t=max(1,floor(floor(基准×刺激值/1000)/200))。'],
 ['刺激时长','刺激值0～1000，刺激耗时为简单120、普通240、专家360tick；完成后扣除等量组织点数，增幅持续时间最多1200tick。'],
 ['组织成本','组织寿命随营养、成长阶段和刺激量变化，不能固定折算个/EU；上表只计算培养液与连杆费用，组织更换和刺激耗电另计。'],
 ['连杆','Magmatter连杆耐久86400，每20tick消耗1点；连杆个/EU列默认无刺激和Magmatter连杆，修改r时也应修改对应材质耐久。'],
 ['废液','废液对应：生物培养液→生物质，精华→生物培养液，龙魂→富集龙息，飞龙→龙素，神龙→飞龙溶剂；不抵扣回收价值。'],
 ['配方说明','当前神龙溶剂每次产出740,000mB，飞龙核心消耗概率50/10000=0.5%；10亿mB对应约6.7568个核心的期望消耗量，仍需备有至少1个核心。'],
];
bio.getRange(`A${bns}`).values=[['计算条件']];bio.getRange(`B${bns}`).values=[['内容']];
for(let i=0;i<bioNotes.length;i++){
 const r=bns+i+1;bio.getRange(`A${r}`).values=[[bioNotes[i][0]]];bio.mergeCells(`B${r}:K${r}`);bio.getRange(`B${r}`).values=[[bioNotes[i][1]]];
 bio.getRange(`B${r}:K${r}`).format.wrapText=true;bio.getRange(`A${r}:K${r}`).format.rowHeight=48;
}
// Widths are sheet-wide. Restore a single layout shared by both biological tables.
const bioWidths=[31,20,32,40,26,26,20,25,28,24,28,44,110];
bioWidths.forEach((v,i)=>bio.getRange(`${col(i)}6:${col(i)}${bns+10}`).format.columnWidth=v);

// Direct production ingredients. Repeated alternative routes stay separate.
const prod=sheets['制造材料'];title(prod,'燃料制造材料成本','仅展开最近一道配方，不抵扣副产物价值；MEU折算对应燃料基准表的100%基准发电配方。','J');
const productions=[];
for(const entry of data.production){
 const r=entry.recipe,o=entry.output;
 const matching=fuels.filter(f=>f.inputs[0].key===o.key);
 const bas=matching[0]; const rb=bas?fuelRow.get(bas.uid):null;
 for(const inp of r.inputs){
  if(!inp.consumed)continue;
  productions.push([label(o),r.type,r.id,label(inp),inp.kind==='fluid'?'mB':'个',inp.qty,inp.chance,o.qty*o.chance,null,null,r.eut??'未指定',r.duration??'未指定',null,ingText(r.outputs.filter(x=>x!==o)),r.expert?'流体专家规则限定':'注册条件见对应源码配方',rb?bas.uid:'无对应基准发电配方','',ref(r),rb]);
 }
}
const pRows=productions.map(x=>x.slice(0,18));
const pt=table(prod,['目标燃料','加工配方类别','配方ID','投入材料','单位','每次材料投入量','消耗概率','每次燃料期望产量','每单位燃料所需材料','每基准MEU所需材料','加工 EU/t','加工时长tick','每单位燃料加工耗电EU','其他产物','注册条件','对应发电配方','','出处'],pRows,[36,39,42,49,10,20,17,22,23,23,20,20,25,85,32,64,3,110],6,'FuelProductionMaterials');
for(let i=0;i<productions.length;i++){
 const r=i+7,rb=productions[i][18];
 formula(prod,`I${r}`,`=F${r}*G${r}/H${r}`);
 if(rb)formula(prod,`J${r}`,`=I${r}*'燃料基准'!L${rb}*1000000`);
 else prod.getRange(`J${r}`).values=[['n.a.']];
 if(typeof productions[i][10]==='number'&&typeof productions[i][11]==='number')formula(prod,`M${r}`,`=K${r}*L${r}/H${r}`);
 else prod.getRange(`M${r}`).values=[['未指定']];
}
prod.getRange(`F7:M${pt.end}`).setNumberFormat('0.000000E+00');prod.getRange(`G7:G${pt.end}`).setNumberFormat('0.0%');prod.freezePanes.freezeColumns(3);

// A compact second table lists nuclear fuels without mislabelling consumed EU as generated EU.
const fission=data.allrecipes.filter(r=>r.type==='FISSION_REACTOR_RECIPES');
const nfStart=otherStart+others.length+4;
table(front,['裂变反应堆燃料','物品投入数','标称时长tick','耗电EU/t','热值FR_HEAT','产物','出处'],fission.map(r=>[label(r.inputs[0]),r.inputs[0].qty,r.duration,r.eut,Number(r.raw.match(/FR_HEAT,\s*(\d+)/)?.[1]),ingText(r.outputs),ref(r)]),[31,46,23,24,22,95,110],nfStart,'FissionFuel');

// Verify a representative updated fuel and difficulty propagation without freezing recipe values.
function expectedSimpleRate(fuel,tier,offset){
 const efficiency=(105-5*tier+offset)/100;
 return fuel.inputs[0].qty/(fuel.eut*Math.floor(fuel.duration*efficiency));
}
st.getRange('B7').values=[[2]];wb.recalculate();
const lvDiesel=specs.find(s=>s.name==='内燃发电机'&&s.config==='LV');
const dieselFuel=fuels.find(f=>f.type==='COMBUSTION_GENERATOR_FUELS'&&f.id==='diesel');
const dieselIndex=comparison.findIndex(r=>r[0]===lvDiesel.name&&r[1]===lvDiesel.config&&r[2]===label(dieselFuel.inputs[0]))+7;
const dieselRate=cmp.getRange(`F${dieselIndex}`).values[0][0];
assert.ok(Math.abs(dieselRate-expectedSimpleRate(dieselFuel,1,0))<1e-12);
const updatedOil=fuels.find(r=>r.type==='SEMI_FLUID_GENERATOR_FUELS'&&r.id==='oil');
const oilCompare=comparison.findIndex(r=>r[0]==='半流质发电机'&&r[1]==='LV'&&r[2]===label(updatedOil.inputs[0]))+7;
assert.ok(Math.abs(cmp.getRange(`F${oilCompare}`).values[0][0]-expectedSimpleRate(updatedOil,1,0))<1e-12);
const bioPower=bio.getRange('E7:E11').values.map(v=>v[0]);
assert.deepEqual(bioPower,[768000000000,12288000000000,62208000000000,196608000000000,480000000000000]);
st.getRange('B7').values=[[3]];wb.recalculate();
const expertRate=cmp.getRange(`F${dieselIndex}`).values[0][0];
assert.ok(Math.abs(expertRate-expectedSimpleRate(dieselFuel,1,-15))<1e-12);
st.getRange('B7').values=[[selectedDifficulty]];wb.recalculate();
console.log(JSON.stringify({fuelDefinitions:fuels.length,machineConfigurations:specs.length,machineFuelRows:comparison.length,productionIngredientRows:productions.length,bioRows:bioMatRows.length,dieselNormal:dieselRate,dieselExpert:expertRate,bioPower}));
const errorScan=await wb.inspect({kind:'match',searchTerm:'#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A|#NUM!|#NULL!|#SPILL!|#CALC!',options:{useRegex:true,maxResults:50},summary:'formula errors',maxChars:4000});
console.log(errorScan.ndjson);
if(/#(?:REF!|DIV\/0!|VALUE!|NAME\?|N\/A|NUM!|NULL!|SPILL!|CALC!)/.test(errorScan.ndjson))throw new Error('公式检查发现错误，停止导出');
console.log((await wb.inspect({kind:'table',range:`燃料对比!A${dieselIndex}:H${dieselIndex}`,include:'values,formulas',tableMaxRows:1,tableMaxCols:8,maxChars:2200})).ndjson);
await fs.mkdir(out,{recursive:true});
const xlsx=await SpreadsheetFile.exportXlsx(wb);await xlsx.save(`${out}/发电机与燃料数据表_中文版.xlsx`);
await fs.writeFile(`${out}/校验结果.json`,JSON.stringify({fuelDefinitions:fuels.length,machineConfigurations:specs.length,machineFuelRows:comparison.length,productionIngredientRows:productions.length,errors:errorScan.ndjson},null,2));
if(previewDir)await fs.mkdir(previewDir,{recursive:true});
for(const [name,range] of (previewDir?[['发电机总览','A1:F15'],['燃料对比',`A${dieselIndex-2}:H${dieselIndex+4}`],['培养液','A1:K11'],['制造材料','A1:J14'],['燃料基准','A1:M14'],['计算条件','A1:C17']]:[])){
 const blob=await wb.render({sheetName:name,range,scale:1.3,format:'png'});await fs.writeFile(`${previewDir}/${name}.png`,new Uint8Array(await blob.arrayBuffer()));
}
console.log('已生成：'+path.join(out,'发电机与燃料数据表_中文版.xlsx'));
