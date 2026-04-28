import assistantImg from '@/assets/images/hero/Cinematic/assistant.png';
import bossImg from '@/assets/images/hero/Cinematic/boss.png';
import danteImg from '@/assets/images/hero/Cinematic/dante.png';
import darrenImg from '@/assets/images/hero/Cinematic/darren.png';
import echoImg from '@/assets/images/hero/Cinematic/echo.png';
import elaraImg from '@/assets/images/hero/Cinematic/elara.png';
import jaxImg from '@/assets/images/hero/Cinematic/jax.png';
import kaiImg from '@/assets/images/hero/Cinematic/kai.png';
import marcusImg from '@/assets/images/hero/Cinematic/marcus.png';
import sableHeroImg from '@/assets/images/hero/Cinematic/sable.png';
import sloaneImg from '@/assets/images/hero/Cinematic/sloane.png';
import valerieImg from '@/assets/images/hero/Cinematic/valerie.png';
import yunaImg from '@/assets/images/hero/Cinematic/yuna.png';

import ciphershadeImg from '@/assets/images/villian/Cinematic/ciphershade.png';
import drRowanImg from '@/assets/images/villian/Cinematic/dr. rowan.png';
import irisImg from '@/assets/images/villian/Cinematic/iris.png';
import junoImg from '@/assets/images/villian/Cinematic/juno.png';
import kaelImg from '@/assets/images/villian/Cinematic/kael.png';
import magnusBossImg from '@/assets/images/villian/Cinematic/magnus boss.png';
import mayaImg from '@/assets/images/villian/Cinematic/maya.png';
import nullbyteImg from '@/assets/images/villian/Cinematic/nullbyte.png';
import redlineghostImg from '@/assets/images/villian/Cinematic/redlineghost.png';
import vexImg from '@/assets/images/villian/Cinematic/vex.png';
import widowbyteImg from '@/assets/images/villian/Cinematic/widowbyte.png';

export type RosterFaction = 'hero' | 'villain';
export type RosterStatus = 'available' | 'locked';

export interface CharacterSkill {
  key: string;
  name: string;
  description: string;
}

export interface Character {
  id: string;
  faction: RosterFaction;
  name: string;
  codename: string;
  title: string;
  role: string;
  background: string;
  status: RosterStatus;
  image: string;
  skills: CharacterSkill[];
  tags: string[];
  backendOperatorId?: string;
}

const skills = (items: string[]): CharacterSkill[] =>
  items.map((item) => ({
    key: item.toLowerCase().replace(/[^a-z0-9]+/g, '-'),
    name: item,
    description: item,
  }));

export const roster: Character[] = [
  // HEROES
  {
    id: 'draven-holt',
    faction: 'hero',
    name: 'Draven Holt',
    codename: 'DARK SYNDICATE',
    title: 'Boss / Overseer',
    role: 'Supreme Commander',
    backendOperatorId: 'op_draven-holt',
    background: 'Draven Holt directs large-scale theater operations through fear, leverage, and strategic control over fractured alliances.',
    status: 'available',
    image: bossImg,
    skills: skills(['Psychological Warfare', 'Black Market Diplomacy', 'Multi-Faction Influence', 'Crisis Manipulation']),
    tags: ['command', 'control', 'influence']
  },
  {
    id: 'kira-vale',
    faction: 'hero',
    name: 'Kira Vale',
    codename: 'NIGHTFALL',
    title: 'Silent Sniper / Infiltrator',
    role: 'Precision Assassin',
    backendOperatorId: 'op_kira-vale',
    background: 'Kira Vale is a surgical hunter built for silent elimination, stealth penetration, and impossible shot placement.',
    status: 'available',
    image: assistantImg,
    skills: skills(['Thermal Cloaking', 'Zero-Sound Sniping', 'Infiltration Hacking', 'Kill-Zone Mapping']),
    tags: ['stealth', 'sniper', 'infiltration']
  },
  {
    id: 'dante-morgana',
    faction: 'hero',
    name: 'Dante Morgana',
    codename: 'ROOTGOD',
    title: 'Zero',
    role: 'System Architect',
    backendOperatorId: 'op_dante-morgana',
    background: 'Dante Morgana designs the battlefield before anyone enters it, turning infrastructure into a weaponized operating system.',
    status: 'available',
    image: danteImg,
    skills: skills(['Zero-Day Engineering', 'Infrastructure Takeover', 'Cyber Battlefield Design', 'Kill-Switch Architecture']),
    tags: ['systems', 'architecture', 'zero-day']
  },
  {
    id: 'elara-voss',
    faction: 'hero',
    name: 'Elara Voss',
    codename: 'ORACLE',
    title: 'Intelligence Analyst',
    role: 'Cyber Intelligence Queen',
    backendOperatorId: 'op_elara-voss',
    background: 'Elara Voss sees threats before they materialize through behavioral modeling and signal anomaly detection.',
    status: 'available',
    image: elaraImg,
    skills: skills(['Behavioral Threat Prediction', 'Signal Pattern Recognition', 'Threat Forecasting', 'Drone Swarm Analytics']),
    tags: ['analyst', 'threat', 'prediction']
  },
  {
    id: 'jax-storm',
    faction: 'hero',
    name: 'Jax Storm',
    codename: 'WARPATH',
    title: 'Command Ops',
    role: 'Tactical Commander',
    backendOperatorId: 'op_jax-storm',
    background: 'Jax Storm coordinates battlefield tempo, route control, and synchronized action across hostile operational zones.',
    status: 'available',
    image: jaxImg,
    skills: skills(['Battlefield Simulation', 'Multi-Team Coordination', 'Electronic Warfare Control', 'Strategic Kill Planning']),
    tags: ['battlefield', 'command', 'ew']
  },
  {
    id: 'kai-chen',
    faction: 'hero',
    name: 'Kai Chen',
    codename: 'SPECTER',
    title: 'Ghost Operative',
    role: 'Phantom Assassin',
    backendOperatorId: 'op_kai-chen',
    background: 'Kai Chen erases presence, neutralizes targets, and exits before the system even registers a breach.',
    status: 'available',
    image: kaiImg,
    skills: skills(['Silent Neutralization', 'Optical Cloaking', 'Zero-Footprint Infiltration', 'Threat Erasure']),
    tags: ['phantom', 'stealth', 'erase']
  },
  {
    id: 'marcus-webb',
    faction: 'hero',
    name: 'Marcus Webb',
    codename: 'BASTION',
    title: 'Security Specialist',
    role: 'Fortress Architect',
    backendOperatorId: 'op_marcus-webb',
    background: 'Marcus Webb builds and holds defensive architecture under extreme pressure, turning chaos into secure ground.',
    status: 'available',
    image: marcusImg,
    skills: skills(['Cyber Defense Architecture', 'Threat Containment', 'Intrusion Prediction', 'Digital Shielding']),
    tags: ['security', 'defense', 'fortress']
  },
  {
    id: 'sable',
    faction: 'hero',
    name: 'Sable',
    codename: 'SERPENT',
    title: 'Malware Architect',
    role: 'Malware Architect',
    backendOperatorId: 'op_sable',
    background: 'Sable writes living malicious code, designing viral intelligence that mutates faster than teams can contain it.',
    status: 'available',
    image: sableHeroImg,
    skills: skills(['Polymorphic Malware Creation', 'Rootkit Engineering', 'AI Virus Design', 'Darknet Exploitation']),
    tags: ['malware', 'rootkit', 'darknet']
  },
  {
    id: 'valerie-cross',
    faction: 'hero',
    name: 'Valerie Cross',
    codename: 'SKYNETA',
    title: 'Strategic Analyst',
    role: 'War Strategy Engine',
    backendOperatorId: 'op_valerie-cross',
    background: 'Valerie Cross turns raw probability into executable combat logic and long-range operational certainty.',
    status: 'available',
    image: valerieImg,
    skills: skills(['Mission Probability Forecasting', 'Tactical Decision Trees', 'Enemy Movement Prediction', 'Risk Matrix Engineering']),
    tags: ['strategy', 'forecasting', 'risk']
  },
  {
    id: 'yuna-park',
    faction: 'hero',
    name: 'Yuna Park',
    codename: 'SIGNAL',
    title: 'Tech Operations',
    role: 'Network Guardian',
    backendOperatorId: 'op_yuna-park',
    background: 'Yuna Park dominates communication layers, controlling how information moves through the battlefield and who gets to hear it.',
    status: 'available',
    image: yunaImg,
    skills: skills(['Secure Communication Systems', 'Battlefield Radio Hacking', 'Tactical Frequency Control', 'Signal Interception']),
    tags: ['signals', 'network', 'guardian']
  },
  {
    id: 'darren-kane',
    faction: 'hero',
    name: 'Darren Kane',
    codename: 'OVERFLOW',
    title: 'Hacking Specialist',
    role: 'Exploit Engineer',
    backendOperatorId: 'op_darren-kane',
    background: 'Darren Kane specializes in low-level exploitation, weaponizing memory, binary structure, and execution flow.',
    status: 'available',
    image: darrenImg,
    skills: skills(['Memory Exploitation', 'Kernel Attacks', 'Code Injection Mastery', 'Binary Weaponization']),
    tags: ['exploit', 'kernel', 'binary']
  },
  {
    id: 'sloane-harper',
    faction: 'hero',
    name: 'Sloane Harper',
    codename: 'RELAY',
    title: 'Operations Communication',
    role: 'Mission Logistics Master',
    backendOperatorId: 'op_sloane-harper',
    background: 'Sloane Harper keeps distributed squads connected, routed, and recoverable through live tactical disruption.',
    status: 'available',
    image: sloaneImg,
    skills: skills(['Real-Time Tactical Routing', 'Field Communication Control', 'Multi-Squad Coordination', 'Emergency Signal Extraction']),
    tags: ['logistics', 'routing', 'comms']
  },
  {
    id: 'echo-13',
    faction: 'hero',
    name: 'Echo-13',
    codename: 'TITAN-CORE',
    title: 'Tactical Robo Unit',
    role: 'AI Combat Machine',
    backendOperatorId: 'op_echo-13',
    background: 'Echo-13 is a hardened autonomous combat chassis built for suppression, survivability, and drone-linked battlefield pressure.',
    status: 'available',
    image: echoImg,
    skills: skills(['Autonomous Combat Algorithms', 'EMP Resistance', 'Heavy Fire Suppression', 'Drone Swarm Control']),
    tags: ['ai', 'combat', 'drone']
  },
  // VILLAIN FACTION – THE SHADOW NETWORK
  {
    id: 'magnus',
    faction: 'villain',
    name: 'Magnus',
    codename: 'BLACK CROWN',
    title: 'The Mastermind',
    role: 'Supreme Villain',
    backendOperatorId: 'op_magnus',
    background: 'Magnus rules the Shadow Network through orchestrated chaos, multi-layered deception, and psychological dominance.',
    status: 'available',
    image: magnusBossImg,
    skills: skills(['Global Chaos Engineering', 'Multi-Layer Strategic Deception', 'Psychological Empire Control']),
    tags: ['mastermind', 'chaos', 'empire']
  },
  {
    id: 'dr-rowan',
    faction: 'villain',
    name: 'Dr. Rowan',
    codename: 'NEUROFORGE',
    title: 'Mindcrypt',
    role: 'Psycho-AI Engineer',
    backendOperatorId: 'op_dr-rowan',
    background: 'Dr. Rowan engineers direct attacks on cognition, memory, and thought architecture through machine-assisted intrusion.',
    status: 'available',
    image: drRowanImg,
    skills: skills(['Neural Hacking', 'BrainComputer Interface Attacks', 'Mind-Control Code', 'Memory Manipulation']),
    tags: ['neural', 'mind', 'ai']
  },
  {
    id: 'ciphershade',
    faction: 'villain',
    name: 'CipherShade',
    codename: 'BLUE REVENANT',
    title: 'Neural Cyber Assassin',
    role: 'Neural Cyber Assassin',
    backendOperatorId: 'op_ciphershade',
    background: 'CipherShade is a lethal cybernetic executioner, combining predictive combat logic with reflex-enhanced kill protocols.',
    status: 'available',
    image: ciphershadeImg,
    skills: skills(['Cybernetic Reflex Boost', 'Neural Kill Protocols', 'Assassin AI Core', 'Combat Prediction']),
    tags: ['assassin', 'neural', 'revenant']
  },
  {
    id: 'iris',
    faction: 'villain',
    name: 'Iris',
    codename: 'MIRAGE',
    title: 'Ghostframe',
    role: 'Reality Distortion Specialist',
    backendOperatorId: 'op_iris',
    background: 'Iris fractures perception itself, turning sensory trust into a weapon against any operator who relies on what they see.',
    status: 'available',
    image: irisImg,
    skills: skills(['Reality Distortion Tech', 'Neural Illusion Injection', 'Perception Hijacking', 'Sensory Manipulation']),
    tags: ['illusion', 'mirage', 'perception']
  },
  {
    id: 'vex',
    faction: 'villain',
    name: 'Vex',
    codename: 'IRONWALL',
    title: 'Enforcer',
    role: 'Tactical Combat AI',
    backendOperatorId: 'op_vex',
    background: 'Vex serves as the armored pressure core of the Shadow Network, intercepting threats and crushing resistance head-on.',
    status: 'available',
    image: vexImg,
    skills: skills(['Tactical Combat AI', 'Threat Interception', 'Heavy Weapon Control', 'Human Shield Protocol']),
    tags: ['enforcer', 'combat', 'interception']
  },
  {
    id: 'juno',
    faction: 'villain',
    name: 'Juno',
    codename: 'CYBERWIDOW',
    title: 'Riftbreaker',
    role: 'Cyber Assassination Specialist',
    backendOperatorId: 'op_juno',
    background: 'Juno specializes in remote execution chains, ghost payloads, and invisible entry into live neural systems.',
    status: 'available',
    image: junoImg,
    skills: skills(['Cyber Assassination', 'Remote Kill Injections', 'Ghost Payload Deployment', 'Neural Backdoor']),
    tags: ['assassination', 'payload', 'backdoor']
  },
  {
    id: 'kael',
    faction: 'villain',
    name: 'Kael',
    codename: 'OBLIVION',
    title: 'ZeroBurn',
    role: 'Infrastructure Annihilator',
    backendOperatorId: 'op_kael',
    background: 'Kael turns entire systems into demolition sites through hybrid cyber-explosive warfare and collapse triggers.',
    status: 'available',
    image: kaelImg,
    skills: skills(['Logic Bomb Engineering', 'Infrastructure Destruction', 'Hybrid Cyber-Explosive Warfare', 'System Meltdown Triggers']),
    tags: ['destruction', 'logic-bomb', 'meltdown']
  },
  {
    id: 'maya-santos',
    faction: 'villain',
    name: 'Maya Santos',
    codename: 'CHAOS QUEEN',
    title: 'Glitch',
    role: 'Reality System Corruptor',
    backendOperatorId: 'op_maya-santos',
    background: 'Maya Santos destabilizes both networks and minds, creating synchronized disorder across digital and physical spaces.',
    status: 'available',
    image: mayaImg,
    skills: skills(['Reality System Corruption', 'Neural Desync Attacks', 'Global Jammer Control', 'Digital Riot Creation']),
    tags: ['chaos', 'glitch', 'corruption']
  },
  {
    id: 'redline-ghost',
    faction: 'villain',
    name: 'Redline Ghost',
    codename: 'VELOCITY',
    title: 'Fast Cyber Agent',
    role: 'Hyper-Speed Infiltrator',
    backendOperatorId: 'op_redline-ghost',
    background: 'Redline Ghost moves faster than response systems can adapt, delivering payloads before defenders can establish context.',
    status: 'available',
    image: redlineghostImg,
    skills: skills(['Hyper-Speed Infiltration', 'Network Sprint Attacks', 'Quantum Routing', 'Instant Payload Drop']),
    tags: ['speed', 'velocity', 'routing']
  },
  {
    id: 'widowbyte',
    faction: 'villain',
    name: 'WidowByte',
    codename: 'DEATHCODE',
    title: 'Hacker Assassin',
    role: 'Silent Execution Malware Specialist',
    backendOperatorId: 'op_widowbyte',
    background: 'WidowByte builds kill-signal traps and execution malware that remain invisible until the target is already compromised.',
    status: 'available',
    image: widowbyteImg,
    skills: skills(['Silent Execution Malware', 'Kill-Signal Programming', 'AI Kill Traps', 'Shadow Injection']),
    tags: ['malware', 'execution', 'shadow']
  },
  {
    id: 'nullbyte',
    faction: 'villain',
    name: 'NullByte',
    codename: 'LOCKLORD',
    title: 'Ransomware Architect',
    role: 'Self-Evolving Extortion Engineer',
    backendOperatorId: 'op_nullbyte',
    background: 'NullByte engineers adaptive ransomware ecosystems that hold systems, data, and institutions hostage at scale.',
    status: 'available',
    image: nullbyteImg,
    skills: skills(['Self-Evolving Ransomware', 'Data Hostage Protocols', 'AI Encryption Loops', 'System Lockdown Triggers']),
    tags: ['ransomware', 'lockdown', 'extortion']
  }
];

export const heroes = roster.filter((character) => character.faction === 'hero');
export const villains = roster.filter((character) => character.faction === 'villain');
