import { Terminal, FolderOpen, Activity, Sparkles, MessageCircle } from "lucide-react";

interface DesktopIconsProps {
  activeApp: string;
  onSelectApp: (app: string) => void;
  onTriggerMatrix: () => void;
}

export default function DesktopIcons({ activeApp, onSelectApp, onTriggerMatrix }: DesktopIconsProps) {
  const iconsList = [
    {
      id: "terminal",
      label: "Terminal Pro",
      sub: "bash shell console",
      icon: Terminal,
      color: "from-emerald-500/20 to-teal-500/10 hover:border-emerald-500/40 border-slate-900",
      iconColor: "text-emerald-400",
    },
    {
      id: "explorer",
      label: "Files Explorer",
      sub: "virtual server disk",
      icon: FolderOpen,
      color: "from-sky-500/20 to-indigo-500/10 hover:border-sky-500/40 border-slate-900",
      iconColor: "text-sky-450",
    },
    {
      id: "monitor",
      label: "System Monitor",
      sub: "contain specs & telemetry",
      icon: Activity,
      color: "from-purple-500/20 to-pink-500/10 hover:border-purple-500/40 border-slate-900",
      iconColor: "text-purple-400",
    },
  ];

  return (
    <div className="flex flex-row md:flex-col items-center justify-center gap-3.5 flex-wrap md:flex-nowrap p-4 bg-slate-950/20 rounded-2xl border border-slate-900/60 backdrop-blur-sm max-w-full overflow-x-auto md:w-36 shrink-0 h-auto">
      {iconsList.map((item) => {
        const IconComponent = item.icon;
        const isActive = activeApp === item.id;
        return (
          <button
            key={item.id}
            onClick={() => onSelectApp(item.id)}
            className={`flex flex-col md:w-full items-center justify-center p-3 rounded-2xl border text-center transition-all cursor-pointer select-none group relative overflow-hidden ${
              isActive 
                ? "bg-gradient-to-br from-sky-500/15 to-indigo-500/5 border-sky-400/50 shadow-md shadow-sky-500/5 scale-[1.02]" 
                : `${item.color} bg-slate-950/40 hover:bg-slate-900/40 hover:-translate-y-0.5`
            }`}
          >
            {/* Soft backdrop lighting on active */}
            {isActive && (
              <span className="absolute inset-0 bg-sky-500/5 rounded-2xl blur-md pointer-events-none" />
            )}

            <div className={`p-2.5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-center mb-1.5 transition-all shadow-inner group-hover:scale-105 ${
              isActive ? "border-sky-500/20 bg-sky-950/20" : ""
            }`}>
              <IconComponent className={`w-5 h-5 ${item.iconColor}`} />
            </div>

            <span className="text-xs font-semibold text-white font-display leading-none tracking-tight block">
              {item.label}
            </span>
            <span className="text-[8px] font-mono uppercase tracking-widest text-slate-500 mt-1 block">
              {item.sub}
            </span>
          </button>
        );
      })}

      {/* Matrix Overlap Action Button */}
      <button
        onClick={onTriggerMatrix}
        className="flex flex-col md:w-full items-center justify-center p-3 rounded-2xl border border-slate-900 bg-slate-950/40 hover:bg-slate-900/40 hover:-translate-y-0.5 text-center transition-all cursor-pointer group relative overflow-hidden from-teal-500/20 via-sky-500/10 hover:border-teal-500/40"
      >
        <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-center mb-1.5 group-hover:scale-105">
          <Sparkles className="w-5 h-5 text-teal-400 animate-pulse" />
        </div>
        <span className="text-xs font-semibold text-white font-display leading-none tracking-tight block">
          AetherMatrix
        </span>
        <span className="text-[8px] font-mono uppercase tracking-widest text-slate-500 mt-1 block">
          digital rain
        </span>
      </button>

    </div>
  );
}
