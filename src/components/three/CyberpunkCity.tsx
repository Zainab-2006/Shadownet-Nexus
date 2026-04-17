import { useRef, useMemo } from 'react';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';

interface BuildingProps {
  position: [number, number, number];
  scale: [number, number, number];
  color: string;
  emissiveIntensity: number;
}

const Building = ({ position, scale, color, emissiveIntensity }: BuildingProps) => {
  const meshRef = useRef<THREE.Mesh>(null);
  
  return (
    <mesh ref={meshRef} position={position} scale={scale}>
      <boxGeometry args={[1, 1, 1]} />
      <meshStandardMaterial
        color={color}
        emissive={color}
        emissiveIntensity={emissiveIntensity}
        metalness={0.8}
        roughness={0.2}
      />
    </mesh>
  );
};

interface WindowLightsProps {
  count: number;
  spread: number;
  height: number;
}

const WindowLights = ({ count, spread, height }: WindowLightsProps) => {
  const points = useMemo(() => {
    const positions = new Float32Array(count * 3);
    const colors = new Float32Array(count * 3);
    
    const cyanColor = new THREE.Color('#00d4ff');
    const magentaColor = new THREE.Color('#ff00aa');
    const whiteColor = new THREE.Color('#ffffff');
    
    for (let i = 0; i < count; i++) {
      positions[i * 3] = (Math.random() - 0.5) * spread;
      positions[i * 3 + 1] = Math.random() * height;
      positions[i * 3 + 2] = (Math.random() - 0.5) * spread;
      
      const colorChoice = Math.random();
      let chosenColor;
      if (colorChoice < 0.5) chosenColor = cyanColor;
      else if (colorChoice < 0.8) chosenColor = magentaColor;
      else chosenColor = whiteColor;
      
      colors[i * 3] = chosenColor.r;
      colors[i * 3 + 1] = chosenColor.g;
      colors[i * 3 + 2] = chosenColor.b;
    }
    
    return { positions, colors };
  }, [count, spread, height]);
  
  return (
    <points>
      <bufferGeometry>
        <bufferAttribute
          attach="attributes-position"
          count={points.positions.length / 3}
          array={points.positions}
          itemSize={3}
        />
        <bufferAttribute
          attach="attributes-color"
          count={points.colors.length / 3}
          array={points.colors}
          itemSize={3}
        />
      </bufferGeometry>
      <pointsMaterial
        size={0.08}
        vertexColors
        transparent
        opacity={0.9}
        sizeAttenuation
      />
    </points>
  );
};

const FloatingParticles = () => {
  const particlesRef = useRef<THREE.Points>(null);
  
  const particles = useMemo(() => {
    const count = 200;
    const positions = new Float32Array(count * 3);
    
    for (let i = 0; i < count; i++) {
      positions[i * 3] = (Math.random() - 0.5) * 30;
      positions[i * 3 + 1] = Math.random() * 15;
      positions[i * 3 + 2] = (Math.random() - 0.5) * 30;
    }
    
    return positions;
  }, []);
  
  useFrame((state) => {
    if (particlesRef.current) {
      particlesRef.current.rotation.y = state.clock.elapsedTime * 0.02;
      const positions = particlesRef.current.geometry.attributes.position.array as Float32Array;
      for (let i = 0; i < positions.length / 3; i++) {
        positions[i * 3 + 1] += Math.sin(state.clock.elapsedTime + i) * 0.002;
      }
      particlesRef.current.geometry.attributes.position.needsUpdate = true;
    }
  });
  
  return (
    <points ref={particlesRef}>
      <bufferGeometry>
        <bufferAttribute
          attach="attributes-position"
          count={particles.length / 3}
          array={particles}
          itemSize={3}
        />
      </bufferGeometry>
      <pointsMaterial
        size={0.05}
        color="#00d4ff"
        transparent
        opacity={0.6}
        sizeAttenuation
      />
    </points>
  );
};

const Ground = () => {
  return (
    <mesh rotation={[-Math.PI / 2, 0, 0]} position={[0, -0.1, 0]}>
      <planeGeometry args={[100, 100]} />
      <meshStandardMaterial
        color="#050510"
        metalness={0.9}
        roughness={0.1}
        emissive="#001020"
        emissiveIntensity={0.2}
      />
    </mesh>
  );
};

export const CyberpunkCity = () => {
  const groupRef = useRef<THREE.Group>(null);
  
  const buildings = useMemo(() => {
    const result: BuildingProps[] = [];
    const colors = ['#001830', '#002040', '#001525', '#002535'];
    
    // Create a grid of buildings
    for (let x = -8; x <= 8; x += 2) {
      for (let z = -8; z <= 2; z += 2) {
        // Skip center area for better view
        if (Math.abs(x) < 3 && z > -4) continue;
        
        const height = 2 + Math.random() * 6;
        const width = 0.6 + Math.random() * 0.8;
        const depth = 0.6 + Math.random() * 0.8;
        
        result.push({
          position: [x + Math.random() * 0.5, height / 2, z + Math.random() * 0.5],
          scale: [width, height, depth],
          color: colors[Math.floor(Math.random() * colors.length)],
          emissiveIntensity: 0.1 + Math.random() * 0.2,
        });
      }
    }
    
    return result;
  }, []);
  
  useFrame((state) => {
    if (groupRef.current) {
      // Subtle camera drift effect
      groupRef.current.rotation.y = Math.sin(state.clock.elapsedTime * 0.1) * 0.05;
    }
  });
  
  return (
    <group ref={groupRef}>
      <ambientLight intensity={0.1} />
      <directionalLight
        position={[10, 20, 5]}
        intensity={0.3}
        color="#4488ff"
      />
      <pointLight position={[0, 10, 0]} intensity={1} color="#00d4ff" distance={30} />
      <pointLight position={[-5, 5, -5]} intensity={0.5} color="#ff00aa" distance={20} />
      <pointLight position={[5, 3, 5]} intensity={0.5} color="#00d4ff" distance={20} />
      
      <fog attach="fog" args={['#050510', 5, 30]} />
      
      <Ground />
      
      {buildings.map((building, i) => (
        <Building key={i} {...building} />
      ))}
      
      <WindowLights count={500} spread={20} height={10} />
      <FloatingParticles />
    </group>
  );
};

export default CyberpunkCity;
