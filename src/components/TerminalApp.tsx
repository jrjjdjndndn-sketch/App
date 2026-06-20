import { useState, useRef, useEffect, KeyboardEvent } from "react";
import { Terminal, Shield, Play } from "lucide-react";

interface TerminalAppProps {
  files: Record<string, string>;
  onWriteFile: (path: string, content: string) => void;
  onDeleteFile: (path: string) => void;
  systemStats: any | null;
  onTriggerMatrix: () => void;
}

export default function TerminalApp({
  files,
  onWriteFile,
  onDeleteFile,
  systemStats,
  onTriggerMatrix
}: TerminalAppProps) {
  const [history, setHistory] = useState<string[]>([
    "Welcome to AetherOS Terminal Suite v3.5 (Unix Core)",
    "Session authenticated. Type 'help' to expand available instructions.",
    "Type 'neofetch' to view system telemetry.",
    ""
  ]);
  const [currentInput, setCurrentInput] = useState("");
  const [commandHistory, setCommandHistory] = useState<string[]>([]);
  const [historyIndex, setHistoryIndex] = useState(-1);
  const [isExecutingRealCommand, setIsExecutingRealCommand] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);
  const pollIntervalRef = useRef<number | null>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [history]);

  useEffect(() => {
    return () => {
      if (pollIntervalRef.current) {
        clearInterval(pollIntervalRef.current);
      }
    };
  }, []);

  const killRunningRealProcess = async () => {
    try {
      const res = await fetch("/api/terminal/kill", { method: "POST" });
      const data = await res.json();
      if (data.success) {
        // Polling loop handles updating states naturally, but we can log user interrupt:
        setHistory((prev) => [...prev, "[SYSTEM] Sending termination signal..."]);
      }
    } catch (e: any) {
      console.error("Failed to kill backend terminal process:", e);
    }
  };

  const runCommandOnRealServer = async (cmdStr: string) => {
    setIsExecutingRealCommand(true);
    setHistory((prev) => [...prev, `debian@aetheros:~$ ${cmdStr}`]);
    setCurrentInput("");

    try {
      const res = await fetch("/api/terminal/run", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ command: cmdStr })
      });
      const data = await res.json();
      if (!data.success) {
        setHistory((prev) => [...prev, `[SERVER EXCEPTION] ${data.error}`, ""]);
        setIsExecutingRealCommand(false);
        return;
      }

      if (pollIntervalRef.current) {
        clearInterval(pollIntervalRef.current);
      }

      pollIntervalRef.current = window.setInterval(async () => {
        try {
          const pollRes = await fetch("/api/terminal/poll");
          const pollData = await pollRes.json();
          if (pollData.success) {
            if (pollData.logs && pollData.logs.length > 0) {
              setHistory((prev) => [...prev, ...pollData.logs.filter((line: string) => line !== null)]);
            }
            if (!pollData.isRunning) {
              if (pollIntervalRef.current) {
                clearInterval(pollIntervalRef.current);
                pollIntervalRef.current = null;
              }
              setIsExecutingRealCommand(false);
              setHistory((prev) => [...prev, ""]);
            }
          }
        } catch (pollErr) {
          console.error("Error during background polling:", pollErr);
        }
      }, 700);

    } catch (err: any) {
      setHistory((prev) => [...prev, `[CONNECTION ERR] Failed to execute: ${err.message}`, ""]);
      setIsExecutingRealCommand(false);
    }
  };

  const handleCommand = (cmdStr: string) => {
    const trimmed = cmdStr.trim();
    
    // Pressing enter / typing when any real process executes will send signal to abort!
    if (isExecutingRealCommand) {
      killRunningRealProcess();
      setCurrentInput("");
      return;
    }

    if (!trimmed) {
      setHistory((prev) => [...prev, "debian@aetheros:~$ "]);
      return;
    }

    const nextCmdHistory = [...commandHistory, trimmed];
    setCommandHistory(nextCmdHistory);
    setHistoryIndex(-1);

    const parts = trimmed.split(" ");
    const command = parts[0].toLowerCase();

    // Client-side visual state overrides (Keep fast routing)
    if (command === "clear") {
      setHistory([]);
      setCurrentInput("");
      return;
    }

    if (command === "matrix") {
      setHistory((prev) => [
        ...prev,
        `debian@aetheros:~$ ${trimmed}`,
        "DIVERGING SYSTEM RENDER INTO DIGITAL CODE FALL...",
        "Initiating local graphics buffer redirect...",
        ""
      ]);
      setCurrentInput("");
      setTimeout(() => {
        onTriggerMatrix();
      }, 500);
      return;
    }

    if (command === "help") {
      const outputLines = [
        "AetherOS Shell v3.5 Real-Time Command Suite",
        "====================================================",
        "help                   Display command overview",
        "matrix                 Trigger matrix screensaver layout",
        "clear                  Clean scroll buffer",
        "",
        "NATIVE SHELL INTERFACES:",
        "Commands execute live inside the Node container's workspace.",
        "Your disk partitions are mounted inside 'drive/'.",
        "Try standard expressions: 'ls', 'pwd', 'whoami', 'uname -a'",
        "Execute Python: 'python3 scripts/miner_agent.py'"
      ];
      setHistory((prev) => [...prev, `debian@aetheros:~$ ${trimmed}`, ...outputLines, ""]);
      setCurrentInput("");
      return;
    }

    // Pass execution to backend
    runCommandOnRealServer(trimmed);
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      handleCommand(currentInput);
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      if (commandHistory.length > 0) {
        const nextIdx = historyIndex === -1 ? commandHistory.length - 1 : Math.max(0, historyIndex - 1);
        setHistoryIndex(nextIdx);
        setCurrentInput(commandHistory[nextIdx]);
      }
    } else if (e.key === "ArrowDown") {
      e.preventDefault();
      if (historyIndex !== -1) {
        const nextIdx = historyIndex + 1;
        if (nextIdx >= commandHistory.length) {
          setHistoryIndex(-1);
          setCurrentInput("");
        } else {
          setHistoryIndex(nextIdx);
          setCurrentInput(commandHistory[nextIdx]);
        }
      }
    }
  };

  return (
    <div className="flex flex-col h-full bg-[#05080f] border border-slate-900 rounded-lg overflow-hidden text-[#4ef0b3] font-mono text-xs selection:bg-emerald-500/25 selection:text-emerald-250">
      
      {/* Tab bar header */}
      <div className="flex items-center justify-between bg-slate-950/90 border-b border-slate-900 px-3 py-1.5 select-none shrink-0 text-slate-400">
        <div className="flex items-center gap-2">
          <Terminal className="w-3.5 h-3.5 text-emerald-400" />
          <span className="font-semibold text-slate-300">Terminal - debian@aetheros: ~</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="text-[10px] bg-emerald-950/60 text-emerald-400 border border-emerald-500/10 px-1.5 rounded">
            PORT: 3000
          </span>
          <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
        </div>
      </div>

      {/* Output screen */}
      <div className="flex-grow p-4 overflow-y-auto space-y-1.5 min-h-0 bg-[#04060b]">
        {history.map((line, idx) => (
          <div key={idx} className="whitespace-pre-wrap leading-relaxed">
            {line.startsWith("debian@aetheros:") ? (
              <span>
                <span className="text-sky-400 font-bold">debian@aetheros</span>
                <span className="text-slate-400">:</span>
                <span className="text-teal-400">~</span>
                <span className="text-slate-200">$</span>{" "}
                <span className="text-white">{line.replace("debian@aetheros:~$ ", "")}</span>
              </span>
            ) : line.includes("SUCCESS:") ? (
              <span className="text-teal-400 font-semibold">{line}</span>
            ) : line.includes("FAILED:") || line.includes("error") || line.includes("not recognized") ? (
              <span className="text-rose-400 font-medium">{line}</span>
            ) : (
              <span className="text-emerald-400/90">{line}</span>
            )}
          </div>
        ))}
        <div ref={bottomRef} />
      </div>

      {/* Input row */}
      <div className="flex items-center gap-2 bg-slate-950 border-t border-slate-900 px-4 py-2 hover:bg-slate-950/80 transition-colors shrink-0">
        <span className="text-sky-450 font-bold">debian@aetheros</span>
        <span className="text-slate-500">:</span>
        <span className="text-teal-500 font-bold">~</span>
        <span className="text-emerald-400">$</span>
        <input
          type="text"
          value={currentInput}
          onChange={(e) => setCurrentInput(e.target.value)}
          onKeyDown={handleKeyDown}
          autoFocus
          className="flex-grow bg-transparent text-white border-none outline-none focus:ring-0 select-text p-0 m-0 leading-none h-4"
          placeholder="Type 'help' for instructions..."
        />
        <Shield className="w-3.5 h-3.5 text-emerald-500/70" />
      </div>
    </div>
  );
}
