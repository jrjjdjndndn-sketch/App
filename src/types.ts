export interface ServerSysStats {
  platform: string;
  arch: string;
  release: string;
  uptime: number;
  cpuModel: string;
  cpuCount: number;
  memory: {
    totalMB: number;
    freeMB: number;
    usedMB: number;
    percent: number;
  };
}

export interface WebApplicationWindow {
  id: string;
  title: string;
  isOpen: boolean;
  isMinimized: boolean;
  isMaximized: boolean;
  zIndex: number;
  x: number;
  y: number;
  width: number;
  height: number;
}
