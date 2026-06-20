import { useState, useEffect } from "react";
import { Cpu, Wifi, Battery, Clock, Terminal, Activity, FolderOpen, RotateCcw } from "lucide-react";

interface DesktopTopBarProps {
  onRefresh: () => void;
  latencyMs: number;
}

export default function DesktopTopBar({ onRefresh, latencyMs }: DesktopTopBarProps) {
  const [timeStr, setTimeStr] = useState("");
  const [batteryLevel, setBatteryLevel] = useState(88);

  useEffect(() => {
    const update = () => {
      const now = new Date();
      setTimeStr(now.toLocaleTimeString("en-US", {
        hour: "numeric",
        minute: "2-digit",
        second: "2-digit",
        hour12: true
      }) + " - " + now.toLocaleDateString("en-US", {
        month: "short",
        day: "2-digit",
        year: "numeric"
      }));
    };
    
    update();
    const handle = setInterval(update, 1000);
    return () => clearInterval(handle);
  }, []);

  return (
    <div className="w-full h-11 bg-[#02050b]/80 backdrop-blur-md border-b border-slate-900 px-4 flex items-center justify-between text-slate-300 select-none text-xs font-sans shrink-0 z-40 relative">
      <div className="flex items-center gap-4.5">
        
        {/* Brand System */}
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-full bg-sky-500 animate-pulse shadow-md" />
          <span className="font-display font-black text-white tracking-widest text-xs uppercase">
            AETHER OS
          </span>
          <span className="text-[10px] font-mono px-1.5 py-0.2 bg-slate-900 border border-slate-800 text-slate-400 rounded">
            v3.5 debian
          </span>
        </div>

        {/* Action shortcut to ping node */}
        <button
          onClick={onRefresh}
          className="hidden md:flex items-center gap-1 text-slate-500 hover:text-sky-400 font-mono transition-colors text-[10px] cursor-pointer"
          title="Recalculate latency & refresh hardware buffers"
        >
          <RotateCcw className="w-3" />
          <span>refresh-all</span>
        </button>

      </div>

      {/* Center Clock Widget */}
      <div className="absolute left-1/2 -translate-x-1/2 flex items-center gap-1.5 font-mono text-xs font-semibold text-white tracking-tight">
        <Clock className="w-3.5 h-3.5 text-sky-400" />
        <span>{timeStr || "06:17 UTC"}</span>
      </div>

      {/* Right side connection / hardware stats */}
      <div className="flex items-center gap-4 text-xs font-mono">
        
        {/* latency indicator */}
        <div className="flex items-center gap-1 bg-slate-950/20 px-2 py-1 rounded border border-slate-900 text-slate-400">
          <Wifi className="w-3 text-emerald-400" />
          <span>PING: <b className="text-emerald-400">{latencyMs}ms</b></span>
        </div>

        {/* Battery widget */}
        <div className="flex items-center gap-1.5">
          <Battery className="w-4 text-slate-400 fill-slate-400/20" />
          <span>{batteryLevel}%</span>
        </div>

        {/* active kernel session */}
        <div className="hidden sm:inline text-slate-500">
          node: <span className="text-teal-400 font-bold">host_container</span>
        </div>

      </div>
    </div>
  );
}
