import { User, Camera, Upload, FileText, X } from "lucide-react";
import { useState } from "react";

export function TAProfile() {
  const [resume, setResume] = useState<File | null>(null);

  const handleResumeUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.[0]) {
      setResume(e.target.files[0]);
    }
  };

  return (
    <div className="p-8 max-w-2xl mx-auto">
      {/* Page title */}
      <div className="mb-8">
        <h2 className="text-[26px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Profile</h2>
        <p className="text-[15px] text-[#86868B] mt-1">Manage your personal information and academic background.</p>
      </div>

      {/* Profile card */}
      <div className="bg-[#F5F5F7] rounded-3xl p-8 space-y-8">
        {/* Avatar row */}
        <div className="flex items-center gap-6">
          <div className="relative flex-shrink-0">
            <div className="w-20 h-20 rounded-full bg-gradient-to-br from-[#C7D2FE] to-[#818CF8] flex items-center justify-center shadow-[0_4px_16px_rgba(129,140,248,0.3)]">
              <User className="w-9 h-9 text-white" strokeWidth={1.5} />
            </div>
            <button className="absolute -bottom-0.5 -right-0.5 w-7 h-7 bg-white rounded-full border border-[#E5E5EA] shadow-sm flex items-center justify-center hover:bg-[#F5F5F7] transition-colors">
              <Camera className="w-3.5 h-3.5 text-[#1D1D1F]" strokeWidth={1.75} />
            </button>
          </div>
          <div>
            <h3 className="text-[20px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Sarah Jenkins</h3>
            <p className="text-[14px] text-[#86868B] mt-0.5">Computer Science, MSc</p>
            <button className="mt-3 px-4 py-1.5 bg-white border border-[#E5E5EA] rounded-full text-[13px] text-[#1D1D1F] hover:bg-[#F0F0F5] transition-all active:scale-95 shadow-sm" style={{ fontWeight: 500 }}>
              Edit Photo
            </button>
          </div>
        </div>

        {/* Divider */}
        <div className="h-px bg-[#E5E5EA]" />

        {/* Form */}
        <form className="space-y-6" onSubmit={(e) => e.preventDefault()}>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex flex-col gap-1.5">
              <label className="text-[12px] text-[#86868B] ml-1 uppercase tracking-wide" style={{ fontWeight: 500 }}>First Name</label>
              <input
                type="text"
                defaultValue="Sarah"
                className="w-full bg-white border border-[#E5E5EA] rounded-xl px-4 py-2.5 text-[15px] text-[#1D1D1F] focus:border-[#0071E3]/40 focus:ring-4 focus:ring-[#0071E3]/8 outline-none transition-all"
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-[12px] text-[#86868B] ml-1 uppercase tracking-wide" style={{ fontWeight: 500 }}>Last Name</label>
              <input
                type="text"
                defaultValue="Jenkins"
                className="w-full bg-white border border-[#E5E5EA] rounded-xl px-4 py-2.5 text-[15px] text-[#1D1D1F] focus:border-[#0071E3]/40 focus:ring-4 focus:ring-[#0071E3]/8 outline-none transition-all"
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-[12px] text-[#86868B] ml-1 uppercase tracking-wide" style={{ fontWeight: 500 }}>Email Address</label>
              <input
                type="email"
                defaultValue="sarah.j@university.edu"
                className="w-full bg-white border border-[#E5E5EA] rounded-xl px-4 py-2.5 text-[15px] text-[#1D1D1F] focus:border-[#0071E3]/40 focus:ring-4 focus:ring-[#0071E3]/8 outline-none transition-all"
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-[12px] text-[#86868B] ml-1 uppercase tracking-wide" style={{ fontWeight: 500 }}>Student ID</label>
              <input
                type="text"
                defaultValue="100456789"
                className="w-full bg-white border border-[#E5E5EA] rounded-xl px-4 py-2.5 text-[15px] text-[#1D1D1F] focus:border-[#0071E3]/40 focus:ring-4 focus:ring-[#0071E3]/8 outline-none transition-all"
              />
            </div>
            <div className="md:col-span-2 flex flex-col gap-1.5">
              <label className="text-[12px] text-[#86868B] ml-1 uppercase tracking-wide" style={{ fontWeight: 500 }}>Academic Background</label>
              <textarea
                rows={4}
                defaultValue="Currently pursuing MSc in Computer Science with a focus on Artificial Intelligence and Machine Learning. Completed BSc in Computer Science with First Class Honors."
                className="w-full bg-white border border-[#E5E5EA] rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] focus:border-[#0071E3]/40 focus:ring-4 focus:ring-[#0071E3]/8 outline-none transition-all resize-none leading-relaxed"
              />
            </div>
          </div>

          {/* Resume Upload Section */}
          <div className="pt-4">
            <div className="h-px bg-[#E5E5EA] mb-6" />
            <label className="text-[12px] text-[#86868B] ml-1 uppercase tracking-wide mb-3 block" style={{ fontWeight: 500 }}>Resume/CV</label>
            
            {resume ? (
              <div className="bg-white border border-[#E5E5EA] rounded-2xl p-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-[#E8F8F0] rounded-xl flex items-center justify-center">
                    <FileText className="w-5 h-5 text-[#34C759]" strokeWidth={1.75} />
                  </div>
                  <div>
                    <p className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>{resume.name}</p>
                    <p className="text-[12px] text-[#86868B]">{(resume.size / 1024).toFixed(0)} KB</p>
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => setResume(null)}
                  className="w-7 h-7 bg-[#F5F5F7] rounded-full flex items-center justify-center hover:bg-[#FFE5E5] hover:text-[#FF3B30] transition-colors"
                >
                  <X className="w-4 h-4" strokeWidth={2} />
                </button>
              </div>
            ) : (
              <label className="block bg-white border-2 border-dashed border-[#D1D1D6] rounded-2xl p-6 text-center cursor-pointer hover:border-[#0071E3] hover:bg-[#F9FBFF] transition-all">
                <div className="w-12 h-12 bg-[#F5F5F7] rounded-2xl flex items-center justify-center mx-auto mb-3">
                  <Upload className="w-5 h-5 text-[#86868B]" strokeWidth={1.75} />
                </div>
                <p className="text-[14px] text-[#1D1D1F] mb-1" style={{ fontWeight: 500 }}>Upload your resume</p>
                <p className="text-[12px] text-[#86868B]">PDF or DOCX · Max 5 MB</p>
                <input
                  type="file"
                  accept=".pdf,.docx"
                  className="hidden"
                  onChange={handleResumeUpload}
                />
              </label>
            )}
          </div>

          {/* Action */}
          <div className="flex justify-end pt-2">
            <button
              type="button"
              className="px-7 py-2.5 bg-[#0071E3] text-white rounded-full text-[14px] hover:bg-[#0077ED] transition-all active:scale-95 shadow-[0_2px_12px_rgba(0,113,227,0.28)]"
              style={{ fontWeight: 500 }}
            >
              Save Changes
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}