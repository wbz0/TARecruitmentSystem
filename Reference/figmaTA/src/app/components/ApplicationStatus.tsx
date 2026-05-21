import { Link } from "react-router";
import { ArrowLeft, Clock, CheckCircle, XCircle } from "lucide-react";

const applications = [
  {
    code: "CS 101",
    title: "Intro to Programming",
    status: "In Review",
    submitted: "Aug 1, 2024",
    icon: Clock,
    dotColor: "bg-[#FF9F0A]",
    badgeColor: "text-[#FF9F0A] bg-[#FFF3E0]",
  },
  {
    code: "BIO 210",
    title: "Human Biology",
    status: "Applied",
    submitted: "Jul 28, 2024",
    icon: CheckCircle,
    dotColor: "bg-[#0071E3]",
    badgeColor: "text-[#0071E3] bg-[#EAF4FF]",
  },
  {
    code: "MATH 302",
    title: "Differential Equations",
    status: "Closed",
    submitted: "Jul 20, 2024",
    icon: XCircle,
    dotColor: "bg-[#C7C7CC]",
    badgeColor: "text-[#86868B] bg-[#F5F5F7]",
  },
];

export function ApplicationStatus() {
  return (
    <div className="p-8 max-w-2xl mx-auto">
      {/* Back */}
      <Link
        to="/"
        className="inline-flex items-center gap-1.5 text-[13px] text-[#0071E3] hover:underline mb-6"
        style={{ fontWeight: 500 }}
      >
        <ArrowLeft className="w-3.5 h-3.5" strokeWidth={2} />
        All Jobs
      </Link>

      <div className="mb-7">
        <h2 className="text-[26px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>My Applications</h2>
        <p className="text-[15px] text-[#86868B] mt-1">Track the status of your submitted applications.</p>
      </div>

      <div className="space-y-3">
        {applications.map((app, idx) => {
          const Icon = app.icon;
          return (
            <div
              key={idx}
              className="flex items-center justify-between bg-[#F5F5F7] rounded-2xl px-6 py-4"
            >
              <div className="flex items-center gap-4">
                <div className={`w-2 h-2 rounded-full flex-shrink-0 ${app.dotColor}`} />
                <div>
                  <p className="text-[15px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>
                    {app.code} – {app.title}
                  </p>
                  <p className="text-[13px] text-[#86868B] mt-0.5">Submitted {app.submitted}</p>
                </div>
              </div>
              <div className={`flex items-center gap-1.5 text-[13px] px-3 py-1.5 rounded-full ${app.badgeColor}`} style={{ fontWeight: 500 }}>
                <Icon className="w-3.5 h-3.5" strokeWidth={2} />
                {app.status}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
