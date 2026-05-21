import { Users, FileText, CheckCircle, Clock, ChevronRight, TrendingUp } from "lucide-react";
import { Link } from "react-router";

const stats = [
  { name: "Active Jobs", value: "12", change: "+2 this week", icon: FileText, accent: "text-[#0071E3] bg-[#EAF4FF]" },
  { name: "Total Applicants", value: "84", change: "+14 this week", icon: Users, accent: "text-[#34C759] bg-[#E8F8F0]" },
  { name: "Pending Review", value: "32", change: "−5 this week", icon: Clock, accent: "text-[#FF9F0A] bg-[#FFF3E0]" },
  { name: "Offers Sent", value: "8", change: "+3 this week", icon: CheckCircle, accent: "text-[#AF52DE] bg-[#F5EEFF]" },
];

const activity = [
  { action: "New application received", course: "CS 101: Intro to Programming", time: "2h ago" },
  { action: "Interview scheduled", course: "CS 350: Operating Systems", time: "5h ago" },
  { action: "Offer accepted", course: "MATH 200: Linear Algebra", time: "1d ago" },
  { action: "Job posting closed", course: "PHYS 102: Physics II", time: "2d ago" },
];

export function MOOverview() {
  return (
    <div className="p-8 max-w-5xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <h2 className="text-[26px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Overview</h2>
        <p className="text-[15px] text-[#86868B] mt-1">Hiring activity across all courses this semester.</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 mb-6">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <div key={stat.name} className="bg-[#F5F5F7] rounded-2xl p-5">
              <div className={`w-9 h-9 rounded-xl flex items-center justify-center mb-4 ${stat.accent}`}>
                <Icon className="w-4.5 h-4.5" strokeWidth={1.75} />
              </div>
              <p className="text-[30px] text-[#1D1D1F] tracking-tight leading-none mb-1" style={{ fontWeight: 600 }}>{stat.value}</p>
              <p className="text-[12px] text-[#86868B]" style={{ fontWeight: 500 }}>{stat.name}</p>
              <div className="flex items-center gap-1 mt-2">
                <TrendingUp className="w-3 h-3 text-[#34C759]" strokeWidth={2} />
                <span className="text-[11px] text-[#34C759]" style={{ fontWeight: 500 }}>{stat.change}</span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Recent activity */}
      <div className="bg-[#F5F5F7] rounded-3xl overflow-hidden">
        <div className="flex items-center justify-between px-6 pt-6 pb-4">
          <p className="text-[13px] text-[#86868B] uppercase tracking-widest" style={{ fontWeight: 500 }}>Recent Activity</p>
          <Link
            to="/mo"
            className="text-[13px] text-[#0071E3] hover:underline flex items-center gap-1"
            style={{ fontWeight: 500 }}
          >
            View all <ChevronRight className="w-3.5 h-3.5" strokeWidth={2} />
          </Link>
        </div>
        <div className="px-2 pb-3 space-y-1">
          {activity.map((item, idx) => (
            <div key={idx} className="flex items-center justify-between bg-white rounded-2xl px-5 py-4">
              <div className="flex items-center gap-4">
                <div className="w-2 h-2 rounded-full bg-[#0071E3] flex-shrink-0" />
                <div>
                  <p className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>{item.action}</p>
                  <p className="text-[13px] text-[#86868B]">{item.course}</p>
                </div>
              </div>
              <span className="text-[12px] text-[#AEAEB2] flex-shrink-0 ml-4">{item.time}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
