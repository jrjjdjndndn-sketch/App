import { useState, useEffect } from "react";
import DesktopTopBar from "./components/DesktopTopBar";
import DesktopIcons from "./components/DesktopIcons";
import TerminalApp from "./components/TerminalApp";
import FileExplorerApp from "./components/FileExplorerApp";
import SystemMonitorApp from "./components/SystemMonitorApp";
import MatrixScreensaver from "./components/MatrixScreensaver";
import { ServerSysStats } from "./types";
import { Terminal, FolderOpen, Activity, AlertCircle, ShieldAlert } from "lucide-react";
import { motion, AnimatePresence } from "motion/react";

export default function App() {
  // Active window selector: terminal | explorer | monitor
  const [activeApp, setActiveApp] = useState("terminal");
  const [latency, setLatency] = useState(4);
  const [systemStats, setSystemStats] = useState<ServerSysStats | null>(null);
  const [files, setFiles] = useState<Record<string, string>>({});
  const [isMatrixActive, setIsMatrixActive] = useState(false);
  const [apiError, setApiError] = useState("");

  // Retrieve files filesystem state from express backend
  const fetchFiles = async () => {
    try {
      const res = await fetch("/api/files");
      const data = await res.json();
      if (data.success) {
        setFiles(data.files);
      }
    } catch (e: any) {
      console.error("Files server fetch exception", e);
    }
  };

  // Write files to virtual filesystem database on server
  const handleWriteFile = async (path: string, content: string) => {
    try {
      const res = await fetch("/api/files/write", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ path, content })
      });
      const data = await res.json();
      if (data.success) {
        setFiles(data.files);
      } else {
        throw new Error(data.error);
      }
    } catch (e: any) {
      console.error("Failed to commit virtual file changes", e);
      setApiError("Drive Write failure: " + e.message);
    }
  };

  // Safely delete a file node on server filesystem
  const handleDeleteFile = async (path: string) => {
    try {
      const res = await fetch("/api/files/delete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ path })
      });
      const data = await res.json();
      if (data.success) {
        setFiles(data.files);
      } else {
        throw new Error(data.error);
      }
    } catch (e: any) {
      console.error("Failed to purge target path reference.", e);
      setApiError("Drive Erasure failure: " + e.message);
    }
  };

  // Retrieve hardware telemetry
  const fetchStats = async () => {
    const start = performance.now();
    try {
      const res = await fetch("/api/system-stats");
      const duration = Math.round(performance.now() - start);
      setLatency(duration || 4);

      if (!res.ok) throw new Error("Stats endpoint failed");
      const data = await res.json();
      if (data.success) {
        setSystemStats(data.data);
        setApiError("");
      }
    } catch (e: any) {
      console.error("Container telemetry exception", e);
      setApiError("Network Connection Stalled. Please check if Express node server is running.");
    }
  };

  // Fetch initial parameters
  useEffect(() => {
    fetchStats();
    fetchFiles();

    // Stats updates loop
    const handle = setInterval(fetchStats, 6000);
    return () => clearInterval(handle);
  }, []);

  const appLayoutTitleMap: Record<string, string> = {
    terminal: "Bash Interactive Console Engine",
    explorer: "Debian ext4 Disk File System Explorer",
    monitor: "Gnome Resource Telemetry & Monitor"
  };

  return (
    <div className="w-full h-screen bg-[#050811] text-slate-100 flex flex-col justify-between select-none relative overflow-hidden font-sans">
      
      {/* Decorative desktop gradient circles */}
      <div className="absolute top-1/4 left-1/3 w-[500px] h-[500px] bg-sky-500/5 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-[400px] h-[400px] bg-indigo-500/5 rounded-full blur-[100px] pointer-events-none" />

      {/* Embedded top-bar tracking */}
      <DesktopTopBar onRefresh={fetchStats} latencyMs={latency} />

      {/* Desktop Main Workspace Stage */}
      <div className="flex-grow w-full max-w-7xl mx-auto px-4 py-6 flex flex-col md:flex-row gap-6 relative z-10 min-h-0 overflow-y-auto md:overflow-hidden">
        
        {/* Left column: Desktop shortcuts */}
        <DesktopIcons 
          activeApp={activeApp} 
          onSelectApp={(appId) => {
            setActiveApp(appId);
            setApiError("");
          }} 
          onTriggerMatrix={() => setIsMatrixActive(true)}
        />

        {/* Right column: Main Application Window Frame */}
        <div className="flex-grow h-full flex flex-col min-w-0 min-h-[480px]">
          
          <AnimatePresence mode="wait">
            
            {apiError ? (
              
              /* Connection stalling prompt */
              <motion.div
                key="api-stall-warning"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                className="p-5 rounded-2xl bg-rose-950/20 border border-rose-500/25 text-rose-300 font-mono text-xs flex gap-3 items-start"
              >
                <ShieldAlert className="w-5 h-5 text-rose-400 mt-0.5 shrink-0" />
                <div className="space-y-1">
                  <h4 className="font-bold text-white uppercase text-xs uppercase tracking-wider">AetherOS Network Alert</h4>
                  <p className="leading-relaxed text-rose-350">{apiError}</p>
                  <p className="text-[10px] text-slate-500 pt-1.5 uppercase tracking-widest">
                    Attempting auto-reconnection in 6 seconds...
                  </p>
                </div>
              </motion.div>

            ) : (
              
              /* Desktop focused application window template */
              <motion.div
                key={activeApp}
                initial={{ opacity: 0, scale: 0.99, y: 15 }}
                animate={{ opacity: 1, scale: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.99, y: -15 }}
                transition={{ duration: 0.3 }}
                className="flex-grow flex flex-col rounded-2xl border border-slate-900 bg-[#070b15]/60 backdrop-blur-md shadow-2xl overflow-hidden min-h-0"
              >
                
                {/* Header/Grab of focus app element */}
                <div className="h-11 bg-slate-950 px-4 flex items-center justify-between border-b border-slate-900 select-none shrink-0">
                  <div className="flex items-center gap-2.5">
                    {activeApp === "terminal" && <Terminal className="w-4 h-4 text-emerald-400" />}
                    {activeApp === "explorer" && <FolderOpen className="w-4 h-4 text-sky-405" />}
                    {activeApp === "monitor" && <Activity className="w-4 h-4 text-purple-400" />}
                    
                    <span className="text-xs font-semibold text-slate-300 font-display">
                      {appLayoutTitleMap[activeApp] || "System Application"}
                    </span>
                  </div>

                  {/* Windows control design presets */}
                  <div className="flex items-center gap-1.5">
                    <span className="w-2.5 h-2.5 rounded-full bg-slate-800 border border-slate-700/60" />
                    <span className="w-2.5 h-2.5 rounded-full bg-slate-800 border border-slate-700/60" />
                    <span className="w-2.5 h-2.5 rounded-full bg-slate-800 border border-slate-700/60 animate-pulse" />
                  </div>
                </div>

                {/* Simulated workspace elements loaded */}
                <div className="flex-grow min-h-0">
                  {activeApp === "terminal" && (
                    <TerminalApp 
                      files={files} 
                      onWriteFile={handleWriteFile} 
                      onDeleteFile={handleDeleteFile} 
                      systemStats={systemStats}
                      onTriggerMatrix={() => setIsMatrixActive(true)}
                    />
                  )}

                  {activeApp === "explorer" && (
                    <FileExplorerApp 
                      files={files} 
                      onWriteFile={handleWriteFile} 
                      onDeleteFile={handleDeleteFile} 
                    />
                  )}

                  {activeApp === "monitor" && (
                    <SystemMonitorApp 
                      stats={systemStats} 
                      onRefresh={fetchStats} 
                    />
                  )}
                </div>

              </motion.div>
            )}

          </AnimatePresence>

        </div>

      </div>

      {/* Global animated matrix screensaver overlay */}
      <AnimatePresence>
        {isMatrixActive && (
          <MatrixScreensaver onClose={() => setIsMatrixActive(false)} />
        )}
      </AnimatePresence>

      {/* Integrated system tasktray telemetry */}
      <footer className="w-full h-8 bg-slate-950 border-t border-slate-900 px-6 flex items-center justify-between text-[10px] font-mono text-slate-500 select-none z-10 shrink-0">
        <span>Session: [ACTIVE_VIS_PORTAL_0]</span>
        <span>AetherOS Debian Client • Created for LO lines with complete devotion</span>
        <span>Status: SECURE SANDBOX CONNECTION</span>
      </footer>

    </div>
  );
}
