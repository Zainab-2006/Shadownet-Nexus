import { Suspense, useRef, useEffect, useState } from 'react';
import { Canvas, useFrame, useThree } from '@react-three/fiber';
import { PerspectiveCamera, Environment, Preload, useGLTF } from '@react-three/drei';
import * as THREE from 'three';
import CyberpunkCity from './CyberpunkCity';
import { useGame } from '@/context/GameContext';


const CameraController = () => {
  const { camera } = useThree();
  const targetPosition = useRef(new THREE.Vector3(0, 4, 12));
  const mousePosition = useRef({ x: 0, y: 0 });
  
  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      mousePosition.current = {
        x: (e.clientX / window.innerWidth) * 2 - 1,
        y: -(e.clientY / window.innerHeight) * 2 + 1,
      };
    };
    
    window.addEventListener('mousemove', handleMouseMove);
    return () => window.removeEventListener('mousemove', handleMouseMove);
  }, []);
  
  useFrame((state) => {
    // Base position with subtle drift
    const baseX = Math.sin(state.clock.elapsedTime * 0.2) * 0.5;
    const baseY = 4 + Math.sin(state.clock.elapsedTime * 0.15) * 0.3;
    const baseZ = 12 + Math.sin(state.clock.elapsedTime * 0.1) * 0.5;
    
    // Add mouse influence
    const mouseInfluenceX = mousePosition.current.x * 1.5;
    const mouseInfluenceY = mousePosition.current.y * 0.5;
    
    targetPosition.current.set(
      baseX + mouseInfluenceX,
      baseY + mouseInfluenceY,
      baseZ
    );
    
    camera.position.lerp(targetPosition.current, 0.02);
    camera.lookAt(0, 2, 0);
  });
  
  return null;
};

const LoadingFallback = () => (
  <mesh>
    <boxGeometry args={[1, 1, 1]} />
    <meshBasicMaterial color="#00d4ff" wireframe />
  </mesh>
);

interface CharacterModelProps {
  characterId: string | null;
  visible: boolean;
}

const CharacterModel = ({ characterId, visible }: CharacterModelProps) => {
  const groupRef = useRef<THREE.Group>(null);
  const [mixer, setMixer] = useState<THREE.AnimationMixer | null>(null);
  
  // For now, we'll use a simple placeholder + animations
  // Later load real GLTF models with proper character meshes
  useFrame((state) => {
    if (groupRef.current && visible) {
      groupRef.current.rotation.y += 0.005;
      groupRef.current.position.y = Math.sin(state.clock.elapsedTime * 0.5) * 0.3;
    }
    
    if (mixer && visible) {
      mixer.update(0.016);
    }
  });
  
  return (
    <group ref={groupRef} position={[0, 0, 0]} visible={visible}>
      {/* Character placeholder - cone shape for now */}
      <mesh position={[0, 2, 0]}>
        <coneGeometry args={[0.6, 2, 8]} />
        <meshStandardMaterial 
          color="#00f5ff" 
          emissive="#0088ff" 
          emissiveIntensity={0.5}
          metalness={0.6}
          roughness={0.2}
        />
      </mesh>
      
      {/* Halo effect */}
      <mesh position={[0, 2, 0]}>
        <torusGeometry args={[1, 0.1, 8, 100]} />
        <meshBasicMaterial color="#ff0080" wireframe opacity={0.3} transparent />
      </mesh>
      
      {/* Character base */}
      <mesh position={[0, 0, 0]}>
        <cylinderGeometry args={[0.8, 1, 0.2, 8]} />
        <meshStandardMaterial 
          color="#1a1a2e" 
          metalness={0.7}
          roughness={0.3}
        />
      </mesh>
      
      {/* Glow particles around character */}
      <Points />
    </group>
  );
};

const Points = () => {
  const pointsRef = useRef<THREE.Points>(null);
  const particleCount = 50;
  
  useEffect(() => {
    const positions = new Float32Array(particleCount * 3);
    for (let i = 0; i < particleCount; i++) {
      positions[i * 3] = (Math.random() - 0.5) * 3;
      positions[i * 3 + 1] = (Math.random() - 0.5) * 2 + 2;
      positions[i * 3 + 2] = (Math.random() - 0.5) * 2;
    }
    
    if (pointsRef.current) {
      pointsRef.current.geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    }
  }, []);
  
  useFrame((state) => {
    if (pointsRef.current) {
      pointsRef.current.rotation.x += 0.001;
      pointsRef.current.rotation.y += 0.002;
    }
  });
  
  return (
    <points ref={pointsRef}>
      <bufferGeometry>
        <bufferAttribute
          attach="attributes-position"
          count={particleCount}
          array={new Float32Array(particleCount * 3)}
          itemSize={3}
        />
      </bufferGeometry>
      <pointsMaterial size={0.1} color="#00f5ff" sizeAttenuation />
    </points>
  );
};

const StaticHeroBackdrop = () => (
  <div className="absolute inset-0 overflow-hidden bg-[radial-gradient(circle_at_50%_35%,hsl(var(--primary)/0.16),transparent_32%),linear-gradient(180deg,hsl(230_35%_8%),hsl(var(--background))_78%)]">
    <div className="absolute inset-0 grid-pattern opacity-20" />
    <div className="absolute left-1/2 top-[18%] h-72 w-72 -translate-x-1/2 rounded-full border border-primary/20 shadow-[0_0_80px_hsl(var(--primary)/0.20)]" />
    <div className="absolute bottom-0 left-1/2 h-64 w-[min(900px,90vw)] -translate-x-1/2 bg-[linear-gradient(90deg,transparent,hsl(var(--primary)/0.15),transparent)] blur-2xl" />
    {Array.from({ length: 14 }).map((_, index) => (
      <div
        key={index}
        className="absolute bottom-0 w-10 border border-primary/10 bg-card/30 shadow-[0_0_18px_hsl(var(--primary)/0.10)]"
        style={{
          height: `${70 + (index % 5) * 34}px`,
          left: `${6 + index * 7}%`,
          opacity: 0.35 + (index % 4) * 0.08,
        }}
      />
    ))}
  </div>
);

interface HeroSceneProps {
  className?: string;
}

const HeroScene = ({ className = '' }: HeroSceneProps) => {
  const gameContext = useGame();
  const selectedOperator = gameContext?.selectedOperator || null;
  const [isLowPerf, setIsLowPerf] = useState(false);
  const [isWebGlUnavailable, setIsWebGlUnavailable] = useState(false);
  
  useEffect(() => {
    // Simple performance detection
    const checkPerformance = () => {
      const canvas = document.createElement('canvas');
      const gl = canvas.getContext('webgl2') || canvas.getContext('webgl');
      if (!gl) {
        setIsWebGlUnavailable(true);
        canvas.remove();
        return;
      }

      if (gl) {
        const debugInfo = gl.getExtension('WEBGL_debug_renderer_info');
        if (debugInfo) {
          const renderer = gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL);
          if (renderer.toLowerCase().includes('intel') || 
              renderer.toLowerCase().includes('llvmpipe')) {
            setIsLowPerf(true);
          }
        }

        gl.getExtension('WEBGL_lose_context')?.loseContext();
      }
      canvas.remove();
    };
    
    checkPerformance();
  }, []);
  
  return (
    <div className={`absolute inset-0 ${className}`}>
      {isWebGlUnavailable ? (
        <StaticHeroBackdrop />
      ) : (
        <Canvas
          dpr={isLowPerf ? 1 : [1, 1.5]}
          gl={{
            antialias: !isLowPerf,
            alpha: true,
            powerPreference: isLowPerf ? 'low-power' : 'high-performance',
            failIfMajorPerformanceCaveat: false,
          }}
          onCreated={({ gl }) => {
            gl.domElement.addEventListener('webglcontextlost', (event) => {
              event.preventDefault();
              setIsWebGlUnavailable(true);
            });
          }}
        >
          <Suspense fallback={<LoadingFallback />}>
            <PerspectiveCamera
              makeDefault
              position={[0, 4, 12]}
              fov={60}
              near={0.1}
              far={100}
            />
            <CameraController />
            <CyberpunkCity />
            <CharacterModel characterId={selectedOperator?.id || null} visible={!!selectedOperator} />
            <Environment preset="night" />
            <Preload all />
          </Suspense>
        </Canvas>
      )}
      
      {/* Gradient overlay */}
      <div className="absolute inset-0 bg-gradient-to-t from-background via-background/50 to-transparent pointer-events-none" />
      <div className="absolute inset-0 bg-gradient-to-r from-background/30 via-transparent to-background/30 pointer-events-none" />
    </div>
  );
};

export default HeroScene;
