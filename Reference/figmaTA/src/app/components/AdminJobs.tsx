import { useState } from "react";
import { Search, MoreVertical, Briefcase, Calendar, Users, DollarSign } from "lucide-react";

interface Job {
  id: string;
  title: string;
  department: string;
  postedBy: string;
  postedDate: string;
  status: "Open" | "Closed" | "Draft";
  applicants: number;
  hourlyRate: number;
}

export function AdminJobs() {
  const [searchQuery, setSearchQuery] = useState("");

  const jobs: Job[] = [
    {
      id: "1",
      title: "CS 101 Teaching Assistant",
      department: "Computer Science",
      postedBy: "Michael Scott",
      postedDate: "2026-03-10",
      status: "Open",
      applicants: 12,
      hourlyRate: 18
    },
    {
      id: "2",
      title: "Data Structures TA",
      department: "Computer Science",
      postedBy: "Jane Smith",
      postedDate: "2026-03-08",
      status: "Open",
      applicants: 8,
      hourlyRate: 20
    },
    {
      id: "3",
      title: "Calculus I Teaching Assistant",
      department: "Mathematics",
      postedBy: "Michael Scott",
      postedDate: "2026-03-05",
      status: "Closed",
      applicants: 15,
      hourlyRate: 17
    },
    {
      id: "4",
      title: "Physics Lab Assistant",
      department: "Physics",
      postedBy: "Jane Smith",
      postedDate: "2026-03-15",
      status: "Open",
      applicants: 5,
      hourlyRate: 19
    },
    {
      id: "5",
      title: "Chemistry Lab TA",
      department: "Chemistry",
      postedBy: "Michael Scott",
      postedDate: "2026-03-01",
      status: "Draft",
      applicants: 0,
      hourlyRate: 18
    },
  ];

  const filteredJobs = jobs.filter(job => 
    job.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
    job.department.toLowerCase().includes(searchQuery.toLowerCase()) ||
    job.postedBy.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getStatusStyle = (status: string) => {
    switch (status) {
      case "Open":
        return "bg-green-50 text-green-700 border-green-200";
      case "Closed":
        return "bg-gray-50 text-gray-700 border-gray-200";
      case "Draft":
        return "bg-yellow-50 text-yellow-700 border-yellow-200";
      default:
        return "bg-gray-50 text-gray-700 border-gray-200";
    }
  };

  const totalApplicants = jobs.reduce((sum, job) => sum + job.applicants, 0);
  const openJobs = jobs.filter(j => j.status === "Open").length;
  const avgRate = jobs.reduce((sum, job) => sum + job.hourlyRate, 0) / jobs.length;

  return (
    <div className="p-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-[28px] text-[#1D1D1F] mb-2" style={{ fontWeight: 600 }}>
          Job Management
        </h1>
        <p className="text-[15px] text-[#86868B]">
          Oversee all job postings across departments
        </p>
      </div>

      {/* Search */}
      <div className="mb-6">
        <div className="relative">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-[#86868B]" strokeWidth={2} />
          <input
            type="text"
            placeholder="Search by job title, department or poster..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-11 pr-4 py-3 bg-[#F5F5F7] border-2 border-transparent rounded-xl text-[14px] text-[#1D1D1F] placeholder:text-[#86868B] focus:outline-none focus:border-[#0071E3] focus:bg-white transition-all"
            style={{ fontWeight: 400 }}
          />
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        <div className="bg-gradient-to-br from-blue-50 to-blue-100/50 rounded-2xl p-5 border border-blue-200/50">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-8 h-8 bg-blue-500 rounded-lg flex items-center justify-center">
              <Briefcase className="w-4 h-4 text-white" strokeWidth={2} />
            </div>
            <span className="text-[13px] text-blue-700" style={{ fontWeight: 500 }}>Total Jobs</span>
          </div>
          <div className="text-[28px] text-blue-900" style={{ fontWeight: 600 }}>
            {jobs.length}
          </div>
        </div>

        <div className="bg-gradient-to-br from-green-50 to-green-100/50 rounded-2xl p-5 border border-green-200/50">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-8 h-8 bg-green-500 rounded-lg flex items-center justify-center">
              <Calendar className="w-4 h-4 text-white" strokeWidth={2} />
            </div>
            <span className="text-[13px] text-green-700" style={{ fontWeight: 500 }}>Open Jobs</span>
          </div>
          <div className="text-[28px] text-green-900" style={{ fontWeight: 600 }}>
            {openJobs}
          </div>
        </div>

        <div className="bg-gradient-to-br from-purple-50 to-purple-100/50 rounded-2xl p-5 border border-purple-200/50">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-8 h-8 bg-purple-500 rounded-lg flex items-center justify-center">
              <Users className="w-4 h-4 text-white" strokeWidth={2} />
            </div>
            <span className="text-[13px] text-purple-700" style={{ fontWeight: 500 }}>Total Applicants</span>
          </div>
          <div className="text-[28px] text-purple-900" style={{ fontWeight: 600 }}>
            {totalApplicants}
          </div>
        </div>

        <div className="bg-gradient-to-br from-orange-50 to-orange-100/50 rounded-2xl p-5 border border-orange-200/50">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-8 h-8 bg-orange-500 rounded-lg flex items-center justify-center">
              <DollarSign className="w-4 h-4 text-white" strokeWidth={2} />
            </div>
            <span className="text-[13px] text-orange-700" style={{ fontWeight: 500 }}>Avg. Rate</span>
          </div>
          <div className="text-[28px] text-orange-900" style={{ fontWeight: 600 }}>
            ${avgRate.toFixed(0)}
          </div>
        </div>
      </div>

      {/* Jobs Table */}
      <div className="bg-white rounded-2xl border border-[#E5E5EA] overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-[#F5F5F7] border-b border-[#E5E5EA]">
                <th className="px-6 py-4 text-left text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Job Title
                </th>
                <th className="px-6 py-4 text-left text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Department
                </th>
                <th className="px-6 py-4 text-left text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Posted By
                </th>
                <th className="px-6 py-4 text-left text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Posted Date
                </th>
                <th className="px-6 py-4 text-left text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Status
                </th>
                <th className="px-6 py-4 text-left text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Applicants
                </th>
                <th className="px-6 py-4 text-left text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Rate
                </th>
                <th className="px-6 py-4 text-right text-[13px] text-[#86868B]" style={{ fontWeight: 500 }}>
                  Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {filteredJobs.map((job) => (
                <tr key={job.id} className="border-b border-[#E5E5EA] hover:bg-[#F5F5F7]/50 transition-colors">
                  <td className="px-6 py-4">
                    <div className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>
                      {job.title}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className="text-[14px] text-[#86868B]">
                      {job.department}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className="text-[14px] text-[#1D1D1F]">
                      {job.postedBy}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className="text-[14px] text-[#1D1D1F]">
                      {new Date(job.postedDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center px-3 py-1 rounded-full text-[12px] border ${getStatusStyle(job.status)}`} style={{ fontWeight: 500 }}>
                      {job.status}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <Users className="w-4 h-4 text-[#86868B]" strokeWidth={1.5} />
                      <span className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>
                        {job.applicants}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 500 }}>
                      ${job.hourlyRate}/hr
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex justify-end">
                      <button className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-[#F5F5F7] transition-colors">
                        <MoreVertical className="w-4 h-4 text-[#86868B]" strokeWidth={2} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {filteredJobs.length === 0 && (
        <div className="text-center py-12">
          <Briefcase className="w-12 h-12 text-[#86868B] mx-auto mb-3" strokeWidth={1.5} />
          <p className="text-[15px] text-[#86868B]">No jobs found</p>
        </div>
      )}
    </div>
  );
}
