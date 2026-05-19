import { useParams, Link } from "react-router";
import { ArrowLeft, User, Check, X, Download, FileText } from "lucide-react";
import { moApplicantsData } from "../data";

function MatchRing({ score }: { score: number }) {
  const color = score >= 90 ? "#34C759" : score >= 80 ? "#0071E3" : "#FF9F0A";
  return (
    <div className="flex flex-col items-center">
      <div
        className="w-16 h-16 rounded-full flex items-center justify-center"
        style={{ background: `conic-gradient(${color} ${score}%, #E5E5EA ${score}%)` }}
      >
        <div className="w-12 h-12 rounded-full bg-white flex items-center justify-center">
          <span className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 700 }}>{score}%</span>
        </div>
      </div>
      <p className="text-[11px] text-[#86868B] mt-1.5 uppercase tracking-wider" style={{ fontWeight: 500 }}>Match</p>
    </div>
  );
}

export function MOApplicantDetail() {
  const { id } = useParams();
  const applicant = moApplicantsData.find((a) => a.id === id);

  if (!applicant) return <div className="p-8 text-[#86868B]">Applicant not found.</div>;

  return (
    <div className="p-8 max-w-2xl mx-auto">
      {/* Back */}
      <Link
        to="/mo"
        className="inline-flex items-center gap-1.5 text-[13px] text-[#0071E3] hover:underline mb-6"
        style={{ fontWeight: 500 }}
      >
        <ArrowLeft className="w-3.5 h-3.5" strokeWidth={2} />
        All Applicants
      </Link>

      {/* Profile card */}
      <div className="bg-[#F5F5F7] rounded-3xl p-7 mb-5">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-5">
            <div className="w-16 h-16 rounded-full bg-gradient-to-br from-[#C7D2FE] to-[#818CF8] flex items-center justify-center shadow-[0_4px_16px_rgba(129,140,248,0.25)]">
              <User className="w-8 h-8 text-white" strokeWidth={1.5} />
            </div>
            <div>
              <h2 className="text-[22px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>{applicant.name}</h2>
              <p className="text-[14px] text-[#86868B] mt-0.5">Applying for CS 101 – Intro to Programming</p>
            </div>
          </div>
          <MatchRing score={applicant.match} />
        </div>
      </div>

      {/* Skills + Documents */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
        <div className="bg-[#F5F5F7] rounded-2xl p-6">
          <p className="text-[12px] text-[#86868B] uppercase tracking-widest mb-4" style={{ fontWeight: 500 }}>Skills</p>
          <div className="flex flex-wrap gap-2">
            {applicant.skills.split(", ").map((skill) => (
              <span
                key={skill}
                className="text-[13px] text-[#1D1D1F] bg-white border border-[#E5E5EA] px-3 py-1 rounded-full shadow-[0_1px_3px_rgba(0,0,0,0.04)]"
                style={{ fontWeight: 500 }}
              >
                {skill}
              </span>
            ))}
          </div>
        </div>
        <div className="bg-[#F5F5F7] rounded-2xl p-6">
          <p className="text-[12px] text-[#86868B] uppercase tracking-widest mb-4" style={{ fontWeight: 500 }}>Documents</p>
          <div className="flex items-center gap-3 bg-white rounded-xl px-4 py-3 border border-[#E5E5EA]">
            <FileText className="w-5 h-5 text-[#0071E3]" strokeWidth={1.75} />
            <div className="flex-1 min-w-0">
              <p className="text-[13px] text-[#1D1D1F] truncate" style={{ fontWeight: 500 }}>
                Resume_{applicant.name.replace(" ", "")}.pdf
              </p>
              <p className="text-[11px] text-[#86868B]">1.2 MB</p>
            </div>
            <button className="flex items-center gap-1 text-[13px] text-[#0071E3] hover:underline flex-shrink-0" style={{ fontWeight: 500 }}>
              <Download className="w-3.5 h-3.5" strokeWidth={1.75} />
              View
            </button>
          </div>
        </div>
      </div>

      {/* Cover letter */}
      <div className="bg-[#F5F5F7] rounded-2xl p-6 mb-6">
        <p className="text-[12px] text-[#86868B] uppercase tracking-widest mb-3" style={{ fontWeight: 500 }}>Cover Letter</p>
        <p className="text-[15px] text-[#1D1D1F] leading-relaxed">
          "I am writing to express my strong interest in the Teaching Assistant position for CS 101.
          With my background in {applicant.skills} and my passion for helping students succeed,
          I believe I would be a great fit for this role…"
        </p>
      </div>

      {/* Actions */}
      <div className="flex gap-3">
        <button className="flex-1 flex items-center justify-center gap-2 bg-[#34C759] text-white rounded-2xl py-3.5 text-[15px] hover:bg-[#2FB350] transition-all active:scale-[0.99] shadow-[0_2px_12px_rgba(52,199,89,0.25)]" style={{ fontWeight: 500 }}>
          <Check className="w-5 h-5" strokeWidth={2.5} />
          Accept Applicant
        </button>
        <button className="flex-1 flex items-center justify-center gap-2 bg-[#F5F5F7] text-[#FF3B30] rounded-2xl py-3.5 text-[15px] hover:bg-[#FFE5E5] transition-all active:scale-[0.99]" style={{ fontWeight: 500 }}>
          <X className="w-5 h-5" strokeWidth={2.5} />
          Reject
        </button>
      </div>
    </div>
  );
}
