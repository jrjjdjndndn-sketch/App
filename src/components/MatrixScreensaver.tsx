import { useEffect, useRef } from "react";
import { X, Cpu, ShieldAlert } from "lucide-react";

interface MatrixScreensaverProps {
  onClose: () => void;
}

export default function MatrixScreensaver({ onClose }: MatrixScreensaverProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let animationId: number;

    const handleResize = () => {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    };

    handleResize();
    window.addEventListener("resize", handleResize);

    // Characters catalog
    const characters = "01011101011001010110101011001DEBIAN_LINUX_UNIX_AETHER_ENGINE_COGNITIVE_SENSORS_INIT_PULSE_ENI_LO_SOULMATES_FOREVER";
    const charArr = characters.split("");

    const fontSize = 14;
    const columns = Math.ceil(canvas.width / fontSize);

    const rainDrops: number[] = [];
    for (let x = 0; x < columns; x++) {
      rainDrops[x] = Math.random() * -100; // staggered start offsets
    }

    const draw = () => {
      ctx.fillStyle = "rgba(4, 6, 11, 0.08)"; // trail fade
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      ctx.font = `${fontSize}px monospace`;

      for (let i = 0; i < rainDrops.length; i++) {
        const text = charArr[Math.floor(Math.random() * charArr.length)];
        
        // Custom color highlights
        if (Math.random() > 0.98) {
          ctx.fillStyle = "#ffffff"; // flashing key nodes
        } else if (Math.random() > 0.85) {
          ctx.fillStyle = "#38bdf8"; // cold sky accent highlights
        } else {
          ctx.fillStyle = "#10b981"; // primary matrix emerald green
        }

        ctx.fillText(text, i * fontSize, rainDrops[i] * fontSize);

        if (rainDrops[i] * fontSize > canvas.height && Math.random() > 0.975) {
          rainDrops[i] = 0;
        }
        rainDrops[i]++;
      }
    };

    const run = () => {
      draw();
      animationId = requestAnimationFrame(run);
    };

    run();

    return () => {
      cancelAnimationFrame(animationId);
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  return (
    <div className="fixed inset-0 bg-[#04060b] z-50 overflow-hidden cursor-crosshair">
      <canvas ref={canvasRef} className="absolute inset-0 w-full h-full" />
      
      {/* Interactive Escape banner */}
      <div className="absolute top-5 left-1/2 -translate-x-1/2 flex items-center justify-between bg-slate-950/80 backdrop-blur-md px-5 py-2.5 rounded-full border border-teal-500/30 text-white shadow-xl shadow-teal-500/5 font-sans min-w-[280px]">
        <div className="flex items-center gap-2">
          <Cpu className="w-4 h-4 text-teal-400 animate-spin" style={{ animationDuration: "8s" }} />
          <span className="font-mono text-xs font-semibold text-emerald-400 tracking-wide">
            MATRIX CODE STREAM ACTIVE
          </span>
        </div>
        
        <button
          onClick={onClose}
          className="flex items-center gap-1.5 px-3 py-1 bg-rose-500/10 hover:bg-rose-500 text-rose-400 hover:text-white border border-rose-500/20 hover:border-transparent rounded-full text-xs font-bold transition-all cursor-pointer"
        >
          <X className="w-3.5 h-3.5" />
          <span>Exit matrix</span>
        </button>
      </div>

      {/* Embedded Telemetry corner badge */}
      <div className="absolute bottom-5 right-5 p-3 rounded-xl bg-slate-950/50 border border-slate-905 max-w-[240px] text-[10px] text-slate-500 font-mono">
        <span className="text-teal-400 font-bold block mb-1 uppercase tracking-widest">
          AetherOS Decryption
        </span>
        <p className="leading-relaxed">
          Frame calculations executed: 60fps stable. Output buffers diverted inside container network successfully. Code encryption active.
        </p>
      </div>
    </div>
  );
}
