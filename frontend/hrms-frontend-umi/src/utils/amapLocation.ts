const AMAP_SCRIPT_ID = 'hrms-amap-js-api';
const AMAP_KEY = '4f21677939703be4507fab830a4bfdff';
const AMAP_SECURITY_CODE = '8c21e3a01992da6dabee157f6c37445b';

export type AmapLocationStatus = 'idle' | 'locating' | 'success' | 'failed';

export interface AmapLocationResult {
  latitude: number;
  longitude: number;
  accuracy?: number;
  locationType?: string;
  isConverted?: boolean;
}

interface AmapPosition {
  lat: number;
  lng: number;
}

interface AmapGeolocationResponse {
  position?: AmapPosition;
  accuracy?: number;
  location_type?: string;
  isConverted?: boolean;
  message?: string;
  originMessage?: string;
}

interface AmapGeolocation {
  getCurrentPosition: (
    callback: (status: 'complete' | 'error', result: AmapGeolocationResponse) => void,
  ) => void;
}

interface AmapConstructor {
  new (options: Record<string, unknown>): AmapGeolocation;
}

interface AmapApi {
  Geolocation: AmapConstructor;
  plugin: (name: string, callback: () => void) => void;
}

declare global {
  interface Window {
    AMap?: AmapApi;
    _AMapSecurityConfig?: {
      securityJsCode: string;
    };
  }
}

let amapScriptPromise: Promise<void> | null = null;

export function loadAmapScript() {
  if (typeof window === 'undefined') {
    return Promise.reject(new Error('高德定位只能在浏览器环境中使用'));
  }

  if (window.AMap?.Geolocation) {
    return Promise.resolve();
  }

  if (amapScriptPromise) {
    return amapScriptPromise;
  }

  window._AMapSecurityConfig = {
    securityJsCode: AMAP_SECURITY_CODE,
  };

  amapScriptPromise = new Promise<void>((resolve, reject) => {
    const existingScript = document.getElementById(AMAP_SCRIPT_ID) as HTMLScriptElement | null;
    if (existingScript) {
      existingScript.addEventListener('load', () => resolve(), { once: true });
      existingScript.addEventListener('error', () => reject(new Error('高德地图脚本加载失败')), {
        once: true,
      });
      return;
    }

    const script = document.createElement('script');
    script.id = AMAP_SCRIPT_ID;
    script.type = 'text/javascript';
    script.async = true;
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}`;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('高德地图脚本加载失败'));
    document.head.appendChild(script);
  });

  return amapScriptPromise;
}

export async function resolveAmapLocation(): Promise<AmapLocationResult> {
  await loadAmapScript();

  return new Promise<AmapLocationResult>((resolve, reject) => {
    if (!window.AMap) {
      reject(new Error('高德地图 API 未初始化'));
      return;
    }

    window.AMap.plugin('AMap.Geolocation', () => {
      const geolocation = new window.AMap!.Geolocation({
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 60000,
        GeoLocationFirst: true,
        getCityWhenFail: true,
        showButton: false,
        showMarker: false,
        showCircle: false,
        panToLocation: false,
        zoomToAccuracy: false,
        convert: true,
      });

      geolocation.getCurrentPosition((status, result) => {
        if (status === 'complete' && result.position) {
          resolve({
            latitude: Number(result.position.lat),
            longitude: Number(result.position.lng),
            accuracy: result.accuracy,
            locationType: result.location_type,
            isConverted: result.isConverted,
          });
          return;
        }

        reject(new Error(result.message || result.originMessage || '高德定位失败'));
      });
    });
  });
}
