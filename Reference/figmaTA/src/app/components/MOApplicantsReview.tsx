import { Link } from "react-router";
import { User, Plus, ChevronRight } from "lucide-react";
import { moApplicantsData } from "../data";

function MatchBar({ score }: { score: number }) {
  const color = score >= 90 ? "#34C759" : score >= 80 ? "#0071E3" : "#FF9F0A";
  return (
    <div className="flex items-center gap-3">
      <div className="flex-1 h-1.5 bg-[#E5E5EA] rounded-full overflow-hidden">
        <div
          className="h-full rounded-full transition-all"
          style={{ width: `${score}%`, backgroundColor: color }}
        />
      </div>
      <span className="text-[13px] text-[#1D1D1F] w-10 text-right flex-shrink-0" style={{ fontWeight: 600 }}>{score}%</span>
    </div>
  );
}

export function MOApplicantsReview() {
  return (
    <div className="p-8 max-w-5xl mx-auto">
      {/* Header */}
      <div className="flex items-end justify-between mb-8">
        <div>
          <h2 className="text-[26px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Applicants</h2>
          <p className="text-[15px] text-[#86868B] mt-1">Review and manage all candidate applications.</p>
        </div>
        <Link
          to="/mo/post-job"
          className="flex items-center gap-2 bg-[#0071E3] text-white px-5 py-2.5 rounded-full text-[14px] hover:bg-[#0077ED] transition-all active:scale-95 shadow-[0_2px_12px_rgba(0,113,227,0.28)]"
          style={{ fontWeight: 500 }}
        >
          <Plus className="w-4 h-4" strokeWidth={2.5} />
          Post Job
        </Link>
      </div>

      {/* Table card */}
      <div className="bg-[#F5F5F7] rounded-3xl overflow-hidden">
        {/* Column headers */}
        <div className="grid grid-cols-[1fr_auto_160px_140px] gap-4 px-6 py-3">
          <span className="text-[11px] text-[#AEAEB2] uppercase tracking-widest" style={{ fontWeight: 500 }}>Candidate</span>
          <span className="text-[11px] text-[#AEAEB2] uppercase tracking-widest" style={{ fontWeight: 500 }}>Skills</span>
          <span className="text-[11px] text-[#AEAEB2] uppercase tracking-widest" style={{ fontWeight: 500 }}>Match Score</span>
          <span className="text-[11px] text-[#AEAEB2] uppercase tracking-widest text-center" style={{ fontWeight: 500 }}>Actions</span>
        </div>

        {/* Rows */}
        <div className="px-2 pb-3 space-y-1.5">
          {moApplicantsData.map((app) => (
            <div
              key={app.id}
              className="grid grid-cols-[1fr_auto_160px_140px] gap-4 items-center bg-white rounded-2xl px-4 py-4"
            >
              {/* Name */}
              <Link to={`/mo/applicant/${app.id}`} className="flex items-center gap-3 group">
                <div className="w-9 h-9 rounded-full bg-[#F5F5F7] border border-[#E5E5EA] flex items-center justify-center flex-shrink-0">
                  <User className="w-4 h-4 text-[#AEAEB2]" strokeWidth={1.75} />
                </div>
                <div>
                  <p className="text-[14px] text-[#1D1D1F] group-hover:text-[#0071E3] transition-colors" style={{ fontWeight: 500 }}>{app.name}</p>
                  <p className="text-[12px] text-[#86868B]">CS 101</p>
                </div>
                <ChevronRight className="w-3.5 h-3.5 text-[#C7C7CC] opacity-0 group-hover:opacity-100 transition-opacity ml-1" strokeWidth={2} />
              </Link>

              {/* Skills */}
              <div className="flex flex-wrap gap-1.5">
                {app.skills.split(", ").map((skill) => (
                  <span
                    key={skill}
                    className="text-[12px] text-[#86868B] bg-[#F5F5F7] border border-[#E5E5EA] px-2.5 py-0.5 rounded-full"
                    style={{ fontWeight: 500 }}
                  >
                    {skill}
                  </span>
                ))}
              </div>

              {/* Match */}
              <MatchBar score={app.match} />

              {/* Actions */}
              <div className="flex items-center gap-2 justify-center">
                <button className="px-4 py-1.5 bg-[#E8F8F0] text-[#34C759] rounded-full text-[12px] hover:bg-[#D1F5E0] transition-colors" style={{ fontWeight: 500 }}>
                  Accept
                </button>
                <button className="px-4 py-1.5 bg-[#F5F5F7] text-[#86868B] rounded-full text-[12px] hover:bg-[#FFE5E5] hover:text-[#FF3B30] transition-colors" style={{ fontWeight: 500 }}>
                  Reject
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
