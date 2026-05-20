import { Link } from "react-router";
import { Search, Clock, Users, ChevronRight } from "lucide-react";
import { jobsData } from "../data";

const statusData = [
  { code: "CS 101", status: "In Review", color: "text-[#FF9F0A] bg-[#FFF3E0]" },
  { code: "BIO 210", status: "Applied", color: "text-[#0071E3] bg-[#EAF4FF]" },
  { code: "MATH 302", status: "Closed", color: "text-[#86868B] bg-[#F5F5F7]" },
];

export function TAJobsDashboard() {
  return (
    <div className="p-8">
      {/* Header row */}
      <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4 mb-8">
        <div>
          <h2 className="text-[26px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Jobs</h2>
          <p className="text-[15px] text-[#86868B] mt-1">Browse and apply for open TA positions.</p>
        </div>
      </div>

      <div className="flex flex-col lg:flex-row gap-6">
        {/* Job list */}
        <div className="flex-1 min-w-0">
          {/* Search */}
          <div className="relative mb-5">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-[#AEAEB2]" strokeWidth={1.75} />
            <input
              type="text"
              placeholder="Search positions…"
              className="w-full bg-[#F5F5F7] border border-transparent rounded-xl pl-11 pr-4 py-3 text-[15px] text-[#1D1D1F] placeholder-[#AEAEB2] focus:bg-white focus:border-[#0071E3]/30 focus:ring-4 focus:ring-[#0071E3]/10 outline-none transition-all"
            />
          </div>

          {/* Section label */}
          <p className="text-[12px] text-[#86868B] uppercase tracking-widest mb-3 ml-1" style={{ fontWeight: 500 }}>Available Positions</p>

          {/* Job cards */}
          <div className="space-y-2.5">
            {jobsData.map((job) => (
              <Link
                key={job.id}
                to={`/job/${job.id}`}
                className="flex items-center justify-between bg-[#F5F5F7] hover:bg-[#EAEAEF] rounded-2xl px-5 py-4 transition-all group"
              >
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 bg-white rounded-xl flex items-center justify-center shadow-[0_1px_4px_rgba(0,0,0,0.06)] flex-shrink-0">
                    <span className="text-[11px] text-[#0071E3]" style={{ fontWeight: 700 }}>{job.code.split(" ")[0]}</span>
                  </div>
                  <div>
                    <p className="text-[15px] text-[#1D1D1F] leading-snug" style={{ fontWeight: 500 }}>
                      {job.code} – {job.title}
                    </p>
                    <p className="text-[13px] text-[#86868B] mt-0.5">{job.semester} · {job.instructor}</p>
                  </div>
                </div>
                <div className="flex items-center gap-5 flex-shrink-0">
                  <div className="hidden sm:flex items-center gap-1.5 text-[13px] text-[#86868B]">
                    <Clock className="w-3.5 h-3.5" strokeWidth={1.75} />
                    <span>{job.hours}</span>
                  </div>
                  <div className="hidden sm:flex items-center gap-1.5 text-[13px] text-[#86868B]">
                    <Users className="w-3.5 h-3.5" strokeWidth={1.75} />
                    <span>{job.applicants}</span>
                  </div>
                  <ChevronRight className="w-4 h-4 text-[#C7C7CC] group-hover:text-[#86868B] transition-colors" strokeWidth={2} />
                </div>
              </Link>
            ))}
          </div>
        </div>

        {/* Status sidebar */}
        <div className="w-full lg:w-64 flex-shrink-0">
          <div className="bg-[#F5F5F7] rounded-2xl p-5">
            <p className="text-[12px] text-[#86868B] uppercase tracking-widest mb-4" style={{ fontWeight: 500 }}>My Applications</p>
            <div className="space-y-2.5">
              {statusData.map((item) => (
                <div key={item.code} className="flex items-center justify-between bg-white rounded-xl px-4 py-3 shadow-[0_1px_3px_rgba(0,0,0,0.04)]">
                  <span className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>{item.code}</span>
                  <span className={`text-[12px] px-2.5 py-0.5 rounded-full ${item.color}`} style={{ fontWeight: 500 }}>
                    {item.status}
                  </span>
                </div>
              ))}
            </div>
            <Link
              to="/status"
              className="mt-4 flex items-center justify-center gap-1.5 text-[13px] text-[#0071E3] hover:underline"
              style={{ fontWeight: 500 }}
            >
              View all
              <ChevronRight className="w-3.5 h-3.5" strokeWidth={2} />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
