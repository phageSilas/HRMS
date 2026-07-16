declare module 'vanta/dist/vanta.waves.min' {
  type VantaWavesOptions = {
    el: HTMLElement;
    THREE: unknown;
    mouseControls?: boolean;
    touchControls?: boolean;
    gyroControls?: boolean;
    minHeight?: number;
    minWidth?: number;
    scale?: number;
    scaleMobile?: number;
    color?: number;
    shininess?: number;
    waveHeight?: number;
    waveSpeed?: number;
    zoom?: number;
  };

  type VantaEffect = {
    destroy: () => void;
  };

  const WAVES: (options: VantaWavesOptions) => VantaEffect;

  export default WAVES;
}
