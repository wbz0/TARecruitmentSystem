import { Link, useNavigate } from "react-router";
import { ArrowLeft } from "lucide-react";

const inputClass = "w-full bg-white border border-[#E5E5EA] rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:border-[#0071E3]/40 focus:ring-4 focus:ring-[#0071E3]/8 outline-none transition-all";
const labelClass = "text-[12px] text-[#86868B] uppercase tracking-wide ml-1";

export function MOPostJob() {
  const navigate = useNavigate();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    navigate("/mo");
  };

  return (
    <div className="p-8 max-w-2xl mx-auto">
      {/* Back */}
      <Link
        to="/mo"
        className="inline-flex items-center gap-1.5 text-[13px] text-[#0071E3] hover:underline mb-6"
        style={{ fontWeight: 500 }}
      >
        <ArrowLeft className="w-3.5 h-3.5" strokeWidth={2} />
        Applicants
      </Link>

      <div className="mb-7">
        <h2 className="text-[26px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Post New Job</h2>
        <p className="text-[15px] text-[#86868B] mt-1">Create a new TA position listing for your course.</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Core info */}
        <div className="bg-[#F5F5F7] rounded-3xl p-7 space-y-5">
          <p className="text-[12px] text-[#86868B] uppercase tracking-widest" style={{ fontWeight: 500 }}>Position Details</p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex flex-col gap-1.5">
              <label className={labelClass} style={{ fontWeight: 500 }}>Job Title</label>
              <input type="text" required placeholder="e.g. Intro to Programming TA" className={inputClass} />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className={labelClass} style={{ fontWeight: 500 }}>Module Code</label>
              <input type="text" required placeholder="e.g. CS 101" className={inputClass} />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className={labelClass} style={{ fontWeight: 500 }}>Instructor</label>
              <input type="text" required placeholder="e.g. Dr. Lee" className={inputClass} />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className={labelClass} style={{ fontWeight: 500 }}>Hours per Week</label>
              <input type="number" required min="1" max="40" placeholder="e.g. 15" className={inputClass} />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className={labelClass} style={{ fontWeight: 500 }}>Application Deadline</label>
              <input type="date" required className={inputClass} />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className={labelClass} style={{ fontWeight: 500 }}>Required Skills</label>
              <input type="text" required placeholder="e.g. Python, SQL" className={inputClass} />
            </div>
          </div>
        </div>

        {/* Description */}
        <div className="bg-[#F5F5F7] rounded-3xl p-7 space-y-4">
          <p className="text-[12px] text-[#86868B] uppercase tracking-widest" style={{ fontWeight: 500 }}>Description</p>
          <textarea
            rows={5}
            required
            placeholder="Describe the role, responsibilities, and any additional expectations…"
            className="w-full bg-white border border-[#E5E5EA] rounded-xl px-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:border-[#0071E3]/40 focus:ring-4 focus:ring-[#0071E3]/8 outline-none transition-all resize-none leading-relaxed"
          />
        </div>

        {/* Submit */}
        <button
          type="submit"
          className="w-full bg-[#0071E3] text-white rounded-2xl py-3.5 text-[15px] hover:bg-[#0077ED] transition-all active:scale-[0.99] shadow-[0_2px_12px_rgba(0,113,227,0.28)]"
          style={{ fontWeight: 500 }}
        >
          Publish Job
        </button>
      </form>
    </div>
  );
}
