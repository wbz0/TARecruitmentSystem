import { useParams, Link } from "react-router";
import { ArrowLeft, Clock, Calendar, BookOpen, Users } from "lucide-react";
import { jobsData } from "../data";

const metaItems = (job: typeof jobsData[0]) => [
  { icon: Calendar, label: "Semester", value: job.semester },
  { icon: Clock, label: "Commitment", value: job.hours },
  { icon: Users, label: "Applicants", value: String(job.applicants) },
  { icon: BookOpen, label: "Deadline", value: job.deadline },
];

export function JobDetail() {
  const { id } = useParams();
  const job = jobsData.find((j) => j.id === id);

  if (!job) return <div className="p-8 text-[#86868B]">Job not found.</div>;

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

      {/* Header */}
      <div className="mb-6">
        <div className="inline-flex items-center bg-[#EAF4FF] text-[#0071E3] text-[12px] px-3 py-1 rounded-full mb-3" style={{ fontWeight: 500 }}>
          {job.code}
        </div>
        <h1 className="text-[26px] text-[#1D1D1F] tracking-tight leading-snug" style={{ fontWeight: 600 }}>{job.title}</h1>
        <p className="text-[15px] text-[#86868B] mt-1.5">Instructor: <span className="text-[#1D1D1F]" style={{ fontWeight: 500 }}>{job.instructor}</span></p>
      </div>

      {/* Meta grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-6">
        {metaItems(job).map(({ icon: Icon, label, value }) => (
          <div key={label} className="bg-[#F5F5F7] rounded-2xl p-4">
            <div className="flex items-center gap-1.5 mb-1.5">
              <Icon className="w-3.5 h-3.5 text-[#AEAEB2]" strokeWidth={1.75} />
              <span className="text-[11px] text-[#86868B] uppercase tracking-wider" style={{ fontWeight: 500 }}>{label}</span>
            </div>
            <p className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>{value}</p>
          </div>
        ))}
      </div>

      {/* Details card */}
      <div className="bg-[#F5F5F7] rounded-3xl p-7 space-y-6">
        <div>
          <h3 className="text-[13px] text-[#86868B] uppercase tracking-widest mb-2" style={{ fontWeight: 500 }}>Responsibilities</h3>
          <p className="text-[15px] text-[#1D1D1F] leading-relaxed">{job.responsibilities}</p>
        </div>

        <div className="h-px bg-[#E5E5EA]" />

        <div>
          <h3 className="text-[13px] text-[#86868B] uppercase tracking-widest mb-3" style={{ fontWeight: 500 }}>Required Skills</h3>
          <div className="flex flex-wrap gap-2">
            {job.requiredSkills.split(", ").map((skill) => (
              <span
                key={skill}
                className="bg-white border border-[#E5E5EA] text-[#1D1D1F] text-[13px] px-3 py-1 rounded-full shadow-[0_1px_3px_rgba(0,0,0,0.04)]"
                style={{ fontWeight: 500 }}
              >
                {skill}
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* CTA */}
      <div className="mt-6 flex justify-end">
        <Link
          to={`/job/${job.id}/apply`}
          className="px-8 py-3 bg-[#0071E3] text-white rounded-full text-[15px] hover:bg-[#0077ED] transition-all active:scale-95 shadow-[0_2px_12px_rgba(0,113,227,0.3)]"
          style={{ fontWeight: 500 }}
        >
          Apply Now
        </Link>
      </div>
    </div>
  );
}
