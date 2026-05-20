import { useState } from "react";
import { useParams, Link, useNavigate } from "react-router";
import { ArrowLeft, UploadCloud, User, FileText, X, CheckCircle2 } from "lucide-react";

const inputClass = "w-full bg-[#F5F5F7] border border-transparent rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:bg-white focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all";
const labelClass = "text-[12px] text-[#86868B] uppercase tracking-wide ml-1";

export function ApplicationForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [file, setFile] = useState<File | null>(null);
  const [dragging, setDragging] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragging(false);
    if (e.dataTransfer.files?.[0]) {
      simulateUpload(e.dataTransfer.files[0]);
    }
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.[0]) {
      simulateUpload(e.target.files[0]);
    }
  };

  const simulateUpload = (selectedFile: File) => {
    setIsUploading(true);
    setUploadProgress(0);
    
    // Simulate upload progress
    const interval = setInterval(() => {
      setUploadProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval);
          setIsUploading(false);
          setFile(selectedFile);
          return 100;
        }
        return prev + 10;
      });
    }, 150);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    navigate("/apply-success");
  };

  return (
    <div className="p-8 max-w-2xl mx-auto">
      {/* Back */}
      <Link
        to={`/job/${id}`}
        className="inline-flex items-center gap-1.5 text-[13px] text-[#0071E3] hover:underline mb-6"
        style={{ fontWeight: 500 }}
      >
        <ArrowLeft className="w-3.5 h-3.5" strokeWidth={2} />
        Back to Job
      </Link>

      <div className="mb-7">
        <h2 className="text-[26px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Application</h2>
        <p className="text-[15px] text-[#86868B] mt-1">Fill in your details to apply for this position.</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Profile picture + basic info */}
        <div className="bg-[#F5F5F7] rounded-3xl p-7">
          <p className="text-[12px] text-[#86868B] uppercase tracking-widest mb-5" style={{ fontWeight: 500 }}>Personal Details</p>
          <div className="flex flex-col sm:flex-row gap-6 items-start">
            {/* Avatar */}
            <div className="flex flex-col items-center gap-2.5 flex-shrink-0">
              <div className="w-20 h-20 rounded-full bg-white border border-[#E5E5EA] flex items-center justify-center hover:bg-[#F0F0F5] cursor-pointer transition-colors shadow-sm">
                <User className="w-8 h-8 text-[#C7C7CC]" strokeWidth={1.5} />
              </div>
              <span className="text-[13px] text-[#0071E3] hover:underline cursor-pointer" style={{ fontWeight: 500 }}>Add photo</span>
            </div>

            {/* Fields */}
            <div className="flex-1 space-y-4 min-w-0">
              <div className="flex flex-col gap-1.5">
                <label className={labelClass} style={{ fontWeight: 500 }}>Full Name</label>
                <input type="text" required placeholder="e.g. John Smith" className={inputClass} />
              </div>
              <div className="flex flex-col gap-1.5">
                <label className={labelClass} style={{ fontWeight: 500 }}>Email Address</label>
                <input type="email" required placeholder="e.g. john@university.edu" className={inputClass} />
              </div>
              <div className="flex flex-col gap-1.5">
                <label className={labelClass} style={{ fontWeight: 500 }}>Phone Number</label>
                <input type="tel" required placeholder="e.g. 123-456-7890" className={inputClass} />
              </div>
            </div>
          </div>
        </div>

        {/* CV upload */}
        <div className="bg-[#F5F5F7] rounded-3xl p-7">
          <p className="text-[12px] text-[#86868B] uppercase tracking-widest mb-5" style={{ fontWeight: 500 }}>Documents</p>
          <div
            onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
            onDragLeave={() => setDragging(false)}
            onDrop={handleDrop}
            className={`border-2 border-dashed rounded-2xl p-8 flex flex-col items-center text-center transition-all cursor-pointer ${
              dragging
                ? "border-[#0071E3] bg-[#EAF4FF]"
                : "border-[#D1D1D6] bg-white hover:border-[#AEAEB2]"
            }`}
          >
            {file ? (
              <div className="flex items-center gap-3">
                <FileText className="w-8 h-8 text-[#0071E3]" strokeWidth={1.5} />
                <div className="text-left">
                  <p className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>{file.name}</p>
                  <p className="text-[12px] text-[#86868B]">{(file.size / 1024).toFixed(0)} KB</p>
                </div>
                <button
                  type="button"
                  onClick={(e) => { e.stopPropagation(); setFile(null); }}
                  className="ml-3 w-6 h-6 bg-[#F5F5F7] rounded-full flex items-center justify-center hover:bg-[#E5E5EA] transition-colors"
                >
                  <X className="w-3.5 h-3.5 text-[#86868B]" strokeWidth={2} />
                </button>
              </div>
            ) : (
              <>
                <div className="w-12 h-12 bg-[#F5F5F7] rounded-2xl flex items-center justify-center mb-3">
                  <UploadCloud className="w-5 h-5 text-[#86868B]" strokeWidth={1.75} />
                </div>
                <p className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>Drop your CV here</p>
                <p className="text-[13px] text-[#86868B] mt-1">or click to browse · PDF or DOCX · Max 5 MB</p>
              </>
            )}
          </div>
          <input
            type="file"
            accept=".pdf,.docx"
            className="hidden"
            onChange={handleFileSelect}
          />
          {isUploading && (
            <div className="mt-4">
              <div className="w-full bg-[#E5E5EA] rounded-full h-2.5">
                <div
                  className="bg-[#0071E3] h-2.5 rounded-full"
                  style={{ width: `${uploadProgress}%` }}
                ></div>
              </div>
              <p className="text-[12px] text-[#86868B] mt-1">Uploading...</p>
            </div>
          )}
        </div>

        {/* Cover letter + portfolio */}
        <div className="bg-[#F5F5F7] rounded-3xl p-7 space-y-5">
          <p className="text-[12px] text-[#86868B] uppercase tracking-widest" style={{ fontWeight: 500 }}>Additional Info</p>
          <div className="flex flex-col gap-1.5">
            <label className={labelClass} style={{ fontWeight: 500 }}>Portfolio URL</label>
            <input type="url" placeholder="https://yourportfolio.com" className={inputClass} />
          </div>
          <div className="flex flex-col gap-1.5">
            <label className={labelClass} style={{ fontWeight: 500 }}>Cover Letter</label>
            <textarea
              rows={5}
              placeholder="Introduce yourself and explain why you're a good fit…"
              className="w-full bg-white border border-[#E5E5EA] rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all resize-none leading-relaxed"
            />
          </div>
        </div>

        {/* Terms + submit */}
        <div className="flex flex-col gap-5">
          <div className="flex items-center gap-3">
            <input
              type="checkbox"
              id="terms"
              required
              className="w-4 h-4 rounded accent-[#0071E3] cursor-pointer"
            />
            <label htmlFor="terms" className="text-[14px] text-[#86868B] cursor-pointer select-none">
              I agree to the <span className="text-[#0071E3]">terms and conditions</span>
            </label>
          </div>
          <button
            type="submit"
            className="w-full bg-[#0071E3] text-white rounded-2xl py-3.5 text-[15px] hover:bg-[#0077ED] transition-all active:scale-[0.99] shadow-[0_2px_12px_rgba(0,113,227,0.28)]"
            style={{ fontWeight: 500 }}
          >
            Submit Application
          </button>
        </div>
      </form>
    </div>
  );
}