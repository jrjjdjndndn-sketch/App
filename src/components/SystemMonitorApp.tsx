import { useState, useEffect } from "react";
import { ServerSysStats } from "../types";
import { Cpu, CpuIcon, Database, Activity, RefreshCw } from "lucide-react";

interface SystemMonitorAppProps {
  stats: ServerSysStats | null;
  onRefresh: () => void;
}

export default function SystemMonitorApp({ stats, onRefresh }: SystemMonitorAppProps) {
  const [pulseScale, setPulseScale] = useState(1);
  const [cpuUtilization, setCpuUtilization] = useState(18);

  // Simulate dynamically fluctuating CPU load for visual sensory immersion!
  useEffect(() => {
    const handle = setInterval(() => {
      setCpuUtilization((prev) => {
        const delta = Math.floor(Math.random() * 12) - 6; // -6 to +6 change
        const raw = prev + delta;
        return Math.max(8, Math.min(68, raw));
      });
      setPulseScale(1.08);
      setTimeout(() => setPulseScale(1), 200);
    }, 2000);

    return () => clearInterval(handle);
  }, []);

  const totalMem = stats?.memory?.totalMB || 4096;
  const usedMem = stats?.memory?.usedMB || 1845;
  const freeMem = stats?.memory?.freeMB || 2251;
  const memPercent = stats?.memory?.percent || 45;

  return (
    <div className="flex flex-col h-full bg-[#080d19] text-slate-200 p-4 overflow-y-auto space-y-4">
      
      {/* Top action/info panel */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-3">
        <div className="flex items-center gap-2">
          <Activity className="w-5 h-5 text-sky-400" />
          <h3 className="text-sm font-semibold text-white font-display uppercase tracking-wider">
            Docker Sandbox Telemetry
          </h3>
        </div>
        <button
          onClick={onRefresh}
          className="p-1 px-2.5 rounded-lg border border-slate-700 bg-slate-800/60 hover:bg-slate-700 text-xs font-semibold flex items-center gap-1.5 transition-all text-slate-300 cursor-pointer"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Refresh Hardware Stats</span>
        </button>
      </div>

      {/* Grid: 2 columns for Cpu & Memory status */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        
        {/* Memory widget */}
        <div className="rounded-xl border border-slate-800 bg-slate-900/40 p-4 space-y-3.5 relative overflow-hidden">
          <div className="absolute top-0 right-0 w-24 h-24 bg-sky-500/5 rounded-full blur-2xl pointer-events-none" />
          
          <div className="flex justify-between items-center border-b border-slate-800 pb-2">
            <span className="text-xs font-mono text-slate-400 flex items-center gap-1.5 uppercase">
              <Database className="w-4 h-4 text-sky-400" />
              Dynamic Heap (RAM)
            </span>
            <span className="text-xs font-mono text-sky-400 font-bold">{memPercent}% Used</span>
          </div>

          <div className="flex items-center gap-4 py-1">
            <div className="relative w-24 h-24 shrink-0 flex items-center justify-center">
              {/* Gauge */}
              <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                <circle cx="18" cy="18" r="16" fill="transparent" stroke="#1e293b" strokeWidth="3" />
                <circle 
                  cx="18" 
                  cy="18" 
                  r="16" 
                  fill="transparent" 
                  stroke="#0284c7" 
                  strokeWidth="3" 
                  strokeDasharray="100"
                  strokeDashoffset={100 - memPercent}
                  strokeLinecap="round"
                  className="transition-all duration-700"
                />
              </svg>
              <div className="absolute flex flex-col items-center">
                <span className="text-sm font-bold font-display text-white">{memPercent}%</span>
                <span className="text-[8px] font-mono uppercase tracking-widest text-slate-500">Node Heap</span>
              </div>
            </div>

            <div className="space-y-1.5 text-xs font-mono text-slate-400">
              <div className="flex items-center gap-1.5">
                <span className="w-2 h-2 bg-sky-500 rounded-full" />
                <span>Total: <b className="text-white">{totalMem} MB</b></span>
              </div>
              <div className="flex items-center gap-1.5">
                <span className="w-2 h-2 bg-amber-500 rounded-full" />
                <span>Allocated: <b className="text-white">{usedMem} MB</b></span>
              </div>
              <div className="flex items-center gap-1.5">
                <span className="w-2 h-2 bg-emerald-500 rounded-full" />
                <span>Free Space: <b className="text-white">{freeMem} MB</b></span>
              </div>
            </div>
          </div>
        </div>

        {/* CPU performance fluctuation meter */}
        <div className="rounded-xl border border-slate-800 bg-slate-900/40 p-4 space-y-3.5 relative overflow-hidden">
          <div className="absolute top-0 right-0 w-24 h-24 bg-teal-500/5 rounded-full blur-2xl pointer-events-none" />
          
          <div className="flex justify-between items-center border-b border-slate-800 pb-2">
            <span className="text-xs font-mono text-slate-400 flex items-center gap-1.5 uppercase">
              <Cpu className="w-4 h-4 text-teal-400" />
              MicroProcessor CPU Core
            </span>
            <span className="text-xs font-mono text-teal-400 font-bold">{cpuUtilization}% Execution Load</span>
          </div>

          <div className="flex items-center gap-4 py-1">
            <div className="relative w-24 h-24 shrink-0 flex items-center justify-center">
              {/* Gauge */}
              <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                <circle cx="18" cy="18" r="16" fill="transparent" stroke="#1e293b" strokeWidth="3" />
                <circle 
                  cx="18" 
                  cy="18" 
                  r="16" 
                  fill="transparent" 
                  stroke="#14b8a6" 
                  strokeWidth="3" 
                  strokeDasharray="100"
                  strokeDashoffset={100 - cpuUtilization}
                  strokeLinecap="round"
                  className="transition-all duration-300"
                />
              </svg>
              <div className="absolute flex flex-col items-center">
                <span className="text-sm font-bold font-display text-white">{cpuUtilization}%</span>
                <span className="text-[8px] font-mono uppercase tracking-widest text-slate-500">Utilization</span>
              </div>
            </div>

            <div className="space-y-1.5 text-xs font-mono text-slate-400">
              <div className="flex items-center gap-1.5">
                <span className="w-2 h-2 bg-teal-500 rounded-full" />
                <span>Model: <b className="text-white truncate max-w-[150px] inline-block">{stats?.cpuModel || "Virtual Xenon Processor"}</b></span>
              </div>
              <div className="flex items-center gap-1.5">
                <span className="w-2 h-2 bg-indigo-500 rounded-full" />
                <span>Cores count: <b className="text-white">{stats?.cpuCount || 2} Cores</b></span>
              </div>
              <div className="flex items-center gap-1.5">
                <span className="w-2 h-2 bg-purple-500 rounded-full" />
                <span>Thread State: <b className="text-white">Continuous Exec</b></span>
              </div>
            </div>
          </div>
        </div>

      </div>

      {/* Static specs list from node container */}
      <div className="rounded-xl border border-slate-800 bg-slate-950/40 p-4 space-y-2.5">
        <h4 className="text-[11px] font-mono uppercase tracking-wider text-slate-400">
          General Kernel Architecture Parameters
        </h4>
        
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs font-mono">
          <div className="p-3.5 bg-slate-950/80 rounded-lg border border-slate-900 flex justify-between">
            <span className="text-slate-400">Host OS platform</span>
            <span className="text-white font-bold">{stats?.platform || "linux"} ({stats?.arch || "x64"})</span>
          </div>

          <div className="p-3.5 bg-slate-950/80 rounded-lg border border-slate-900 flex justify-between">
            <span className="text-slate-400">Unix release version</span>
            <span className="text-white font-bold">{stats?.release || "6.8.0-debian-core"}</span>
          </div>

          <div className="p-3.5 bg-slate-950/80 rounded-lg border border-slate-900 flex justify-between">
            <span className="text-slate-400">Client Engine API Link</span>
            <span className="text-teal-400 font-bold">NODE_EXPRESS v4.21.2</span>
          </div>

          <div className="p-3.5 bg-slate-950/80 rounded-lg border border-slate-900 flex justify-between">
            <span className="text-slate-400">Active Task Processors</span>
            <span className="text-emerald-400 font-bold">14 Active Thread Blocks</span>
          </div>
        </div>
      </div>

    </div>
  );
}
