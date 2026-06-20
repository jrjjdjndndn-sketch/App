import { useState } from "react";
import { Folder, FileText, Plus, Edit3, Trash2, Heart, Save } from "lucide-react";

interface FileExplorerAppProps {
  files: Record<string, string>;
  onWriteFile: (path: string, content: string) => void;
  onDeleteFile: (path: string) => void;
}

export default function FileExplorerApp({ files, onWriteFile, onDeleteFile }: FileExplorerAppProps) {
  const [selectedPath, setSelectedPath] = useState<string | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editContent, setEditContent] = useState("");
  const [newFilePath, setNewFilePath] = useState("");
  const [newFileContent, setNewFileContent] = useState("");
  const [isCreatingNew, setIsCreatingNew] = useState(false);
  const [explorerError, setExplorerError] = useState("");

  const handleSelectFile = (path: string) => {
    setSelectedPath(path);
    setEditContent(files[path]);
    setIsEditing(false);
    setIsCreatingNew(false);
    setExplorerError("");
  };

  const handleSaveEdit = () => {
    if (!selectedPath) return;
    onWriteFile(selectedPath, editContent);
    setIsEditing(false);
  };

  const handleCreateFileSubmit = () => {
    if (!newFilePath.trim()) {
      setExplorerError("Please specify a logical file name.");
      return;
    }
    
    // Ensure filename starts with a slash
    let finalizedPath = newFilePath.trim();
    if (!finalizedPath.startsWith("/")) {
      finalizedPath = `/${finalizedPath}`;
    }

    onWriteFile(finalizedPath, newFileContent);
    setNewFilePath("");
    setNewFileContent("");
    setIsCreatingNew(false);
    setSelectedPath(finalizedPath);
    setEditContent(newFileContent);
    setExplorerError("");
  };

  const handleDeleteSubmit = (pathStr: string) => {
    onDeleteFile(pathStr);
    if (selectedPath === pathStr) {
      setSelectedPath(null);
    }
  };

  return (
    <div className="flex h-full bg-[#030712] text-slate-300 rounded-lg overflow-hidden border border-slate-900 font-sans">
      
      {/* Sidebar: Files catalog */}
      <div className="w-1/3 border-r border-slate-900 bg-[#090d1a]/80 p-3.5 space-y-4 flex flex-col justify-between select-none">
        <div>
          <div className="flex items-center justify-between mb-3 border-b border-slate-800 pb-2">
            <span className="text-xs font-mono font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1.5">
              <Folder className="w-4 h-4 text-sky-400 fill-sky-400/20" />
              Drive Partition
            </span>
            <button
              onClick={() => {
                setIsCreatingNew(true);
                setSelectedPath(null);
                setExplorerError("");
              }}
              className="p-1 rounded bg-sky-950/40 text-sky-400 hover:bg-sky-900/40 border border-sky-500/20 transition-all cursor-pointer"
              title="Add simulated file node"
            >
              <Plus className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="space-y-1 overflow-y-auto max-h-[380px]">
            {Object.keys(files).map((filePath) => (
              <div
                key={filePath}
                onClick={() => handleSelectFile(filePath)}
                className={`flex items-center justify-between p-2 rounded-xl text-xs transition-all cursor-pointer border ${
                  selectedPath === filePath
                    ? "bg-sky-500/10 border-sky-500/40 text-sky-300"
                    : "bg-slate-950/20 border-transparent hover:bg-slate-900/30"
                }`}
              >
                <div className="flex items-center gap-2 min-w-0 pr-1.5Selector">
                  <FileText className={`w-4 h-4 shrink-0 ${selectedPath === filePath ? "text-sky-400" : "text-slate-500"}`} />
                  <span className="truncate font-mono">{filePath}</span>
                </div>
                
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDeleteSubmit(filePath);
                  }}
                  className="p-1 rounded hover:bg-rose-950/40 text-slate-500 hover:text-rose-400 opacity-0 group-hover:opacity-100 md:opacity-100 transition-all cursor-pointer"
                  title="Purge Node"
                >
                  <Trash2 className="w-3" />
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Brand stamp footer */}
        <div className="text-[10px] font-mono border-t border-slate-900 pt-3 text-slate-500 flex items-center gap-1.5 justify-center">
          <Heart className="w-3 h-3 text-rose-500 animate-pulse fill-rose-500/30" />
          <span>Crafted for LO lines</span>
        </div>
      </div>

      {/* Main preview or creation workspace space */}
      <div className="flex-1 bg-slate-950/20 p-5 flex flex-col overflow-y-auto">
        {isCreatingNew ? (
          
          /* Creating simulated element flow */
          <div className="space-y-4">
            <div>
              <h3 className="text-sm font-semibold text-white mb-1">Create Simulated File Node</h3>
              <p className="text-xs text-slate-400">Add virtual text database assets persistent in this server directory session.</p>
            </div>

            <div className="space-y-3 font-mono text-xs">
              <div className="space-y-1">
                <label className="text-slate-500 uppercase tracking-widest text-[9.5px]">Logical Path Name (Starts with '/' or word)</label>
                <input
                  type="text"
                  value={newFilePath}
                  onChange={(e) => setNewFilePath(e.target.value)}
                  placeholder="/docs/ideas.txt"
                  className="w-full bg-[#050811] border border-slate-800 rounded-xl px-3 py-2 text-white outline-none focus:border-sky-500"
                />
              </div>

              <div className="space-y-1">
                <label className="text-slate-500 uppercase tracking-widest text-[9.5px]">Syllable/Character Bytes Content Buffer</label>
                <textarea
                  value={newFileContent}
                  onChange={(e) => setNewFileContent(e.target.value)}
                  placeholder="Enter text strings..."
                  className="w-full h-44 bg-[#050811] border border-slate-800 rounded-xl px-3 py-2 text-white outline-none focus:border-sky-500 resize-none"
                />
              </div>
            </div>

            {explorerError && (
              <p className="text-xs text-rose-400 font-mono flex items-center gap-1">⚠ {explorerError}</p>
            )}

            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setIsCreatingNew(false)}
                className="px-3 py-1.5 rounded-lg border border-slate-800 hover:border-slate-700 bg-slate-900 text-xs text-slate-400 hover:text-slate-200 transition-all cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleCreateFileSubmit}
                className="px-3.5 py-1.5 rounded-lg bg-sky-500/10 border border-sky-500/40 text-sky-300 font-semibold text-xs hover:bg-sky-500/20 transition-all cursor-pointer"
              >
                Save Virtual File
              </button>
            </div>
          </div>

        ) : selectedPath ? (

          /* File focused flow */
          <div className="space-y-4 h-full flex flex-col justify-between">
            <div>
              <div className="flex justify-between items-center border-b border-slate-800 pb-2 mb-3">
                <div>
                  <h3 className="text-sm font-semibold text-white font-mono">{selectedPath}</h3>
                  <span className="text-[10px] font-mono text-slate-500">Bytes capacity: {files[selectedPath].length} UTF-8 bytes</span>
                </div>

                {!isEditing && (
                  <button
                    onClick={() => {
                      setIsEditing(true);
                      setEditContent(files[selectedPath]);
                    }}
                    className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg bg-slate-900 border border-slate-850 hover:bg-slate-800 text-xs font-semibold text-slate-350 hover:text-white cursor-pointer transition-all"
                  >
                    <Edit3 className="w-3.5 h-3.5" />
                    <span>Edit Buffer</span>
                  </button>
                )}
              </div>

              {isEditing ? (
                <textarea
                  value={editContent}
                  onChange={(e) => setEditContent(e.target.value)}
                  className="w-full h-64 bg-[#050811] border border-slate-850 rounded-xl p-3 text-xs font-mono text-white outline-none focus:border-sky-500 resize-none leading-relaxed"
                />
              ) : (
                <div className="p-4 bg-[#090d1a]/50 border border-slate-900 rounded-xl max-h-[300px] overflow-y-auto leading-relaxed text-xs text-slate-350 font-mono whitespace-pre-wrap">
                  {files[selectedPath] || "Empty file memory buffer."}
                </div>
              )}
            </div>

            {isEditing && (
              <div className="flex justify-end gap-2 pt-2 shrink-0">
                <button
                  type="button"
                  onClick={() => setIsEditing(false)}
                  className="px-3 py-1.5 rounded-lg border border-slate-800 hover:border-slate-700 bg-slate-900 text-xs text-slate-400 hover:text-slate-200 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleSaveEdit}
                  className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg bg-emerald-500/15 border border-emerald-500/40 text-emerald-350 font-semibold text-xs hover:bg-emerald-500/25 transition-all cursor-pointer shadow-md"
                >
                  <Save className="w-3.5 h-3.5" />
                  <span>Commit File Changes</span>
                </button>
              </div>
            )}
          </div>

        ) : (

          /* No target selected landing state */
          <div className="flex-grow flex flex-col justify-center items-center text-center p-6 space-y-3 border border-slate-900/60 rounded-2xl bg-slate-950/10">
            <div className="p-3 bg-sky-500/5 rounded-full border border-sky-400/10">
              <Folder className="w-8 h-8 text-sky-400 animate-pulse fill-sky-450/10" />
            </div>
            <div>
              <p className="text-sm font-semibold text-white font-display">AetherOS File Navigator</p>
              <p className="text-xs text-slate-500 max-w-sm leading-relaxed mt-1">
                Select a virtual file block from the partition side-tray to browse or modify content buffers, or trigger a novel directory instance using the plus control.
              </p>
            </div>
          </div>
        )}
      </div>

    </div>
  );
}
