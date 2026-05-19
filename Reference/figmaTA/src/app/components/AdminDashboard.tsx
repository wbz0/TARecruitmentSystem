import { Users, Briefcase, CheckCircle2, Clock, TrendingUp, TrendingDown } from "lucide-react";
import { AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from "recharts";

const statsData = [
  { icon: Users, label: "Total Applicants", value: "248", change: "+12%", trend: "up", color: "#0071E3" },
  { icon: Briefcase, label: "Active Positions", value: "18", change: "+3", trend: "up", color: "#34C759" },
  { icon: CheckCircle2, label: "Hired this Month", value: "32", change: "+8%", trend: "up", color: "#FF9F0A" },
  { icon: Clock, label: "Pending Reviews", value: "45", change: "-5", trend: "down", color: "#FF3B30" },
];

const applicationTrendData = [
  { month: "Jan", applications: 32 },
  { month: "Feb", applications: 45 },
  { month: "Mar", applications: 38 },
  { month: "Apr", applications: 52 },
  { month: "May", applications: 61 },
  { month: "Jun", applications: 58 },
];

const departmentData = [
  { name: "Computer Science", value: 85, color: "#0071E3" },
  { name: "Mathematics", value: 48, color: "#34C759" },
  { name: "Physics", value: 42, color: "#FF9F0A" },
  { name: "Engineering", value: 73, color: "#FF3B30" },
];

const workloadData = [
  { mo: "Dr. Smith", pending: 12, reviewed: 28, hired: 8 },
  { mo: "Dr. Johnson", pending: 8, reviewed: 35, hired: 12 },
  { mo: "Dr. Williams", pending: 15, reviewed: 22, hired: 6 },
  { mo: "Dr. Brown", pending: 10, reviewed: 30, hired: 10 },
];

const recentActivity = [
  { applicant: "Emma Wilson", action: "Application accepted", job: "CS 101 TA", time: "2 minutes ago", status: "accepted" },
  { applicant: "James Chen", action: "Application submitted", job: "Math TA", time: "15 minutes ago", status: "pending" },
  { applicant: "Sarah Johnson", action: "Interview scheduled", job: "Physics Lab TA", time: "1 hour ago", status: "interview" },
  { applicant: "Michael Brown", action: "Application rejected", job: "Web Dev TA", time: "3 hours ago", status: "rejected" },
];

export function AdminDashboard() {
  return (
    <div className="p-8 max-w-7xl mx-auto">
      <div className="mb-8">
        <h2 className="text-[26px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Admin Dashboard</h2>
        <p className="text-[15px] text-[#86868B] mt-1">Overview of recruitment statistics and MO workload.</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5 mb-6">
        {statsData.map((stat, index) => {
          const Icon = stat.icon;
          const isPositive = stat.trend === "up";
          const TrendIcon = isPositive ? TrendingUp : TrendingDown;
          return (
            <div key={index} className="bg-[#F5F5F7] rounded-3xl p-6">
              <div className="flex items-start justify-between mb-4">
                <div className="w-11 h-11 rounded-2xl flex items-center justify-center" style={{ backgroundColor: `${stat.color}15` }}>
                  <Icon className="w-5 h-5" style={{ color: stat.color }} strokeWidth={1.75} />
                </div>
                <div className={`flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] ${isPositive ? "bg-[#E8F8F0] text-[#34C759]" : "bg-[#FFE5E5] text-[#FF3B30]"}`} style={{ fontWeight: 600 }}>
                  <TrendIcon className="w-3 h-3" strokeWidth={2.5} />
                  {stat.change}
                </div>
              </div>
              <h3 className="text-[32px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 700 }}>{stat.value}</h3>
              <p className="text-[13px] text-[#86868B] mt-1" style={{ fontWeight: 500 }}>{stat.label}</p>
            </div>
          );
        })}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        {/* Application Trend */}
        <div className="lg:col-span-2 bg-[#F5F5F7] rounded-3xl p-7">
          <div className="mb-5">
            <h3 className="text-[18px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Application Trends</h3>
            <p className="text-[13px] text-[#86868B] mt-1">Monthly application volume over time</p>
          </div>
          <ResponsiveContainer width="100%" height={280}>
            <AreaChart data={applicationTrendData}>
              <defs>
                <linearGradient id="colorApplications" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#0071E3" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#0071E3" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#E5E5EA" />
              <XAxis dataKey="month" tick={{ fill: "#86868B", fontSize: 12, fontWeight: 500 }} />
              <YAxis tick={{ fill: "#86868B", fontSize: 12 }} />
              <Tooltip
                contentStyle={{
                  backgroundColor: "white",
                  border: "none",
                  borderRadius: 12,
                  boxShadow: "0 4px 16px rgba(0,0,0,0.1)",
                  fontSize: 13,
                  fontWeight: 500,
                }}
                cursor={{ stroke: "#0071E3", strokeWidth: 1, strokeDasharray: "5 5" }}
              />
              <Area type="monotone" dataKey="applications" stroke="#0071E3" strokeWidth={2} fillOpacity={1} fill="url(#colorApplications)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Department Distribution */}
        <div className="bg-[#F5F5F7] rounded-3xl p-7">
          <div className="mb-5">
            <h3 className="text-[18px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>By Department</h3>
            <p className="text-[13px] text-[#86868B] mt-1\">Applicant distribution</p>
          </div>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie
                data={departmentData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={80}
                paddingAngle={3}
                dataKey="value"
              >
                {departmentData.map((entry, index) => (
                  <Cell key={`dept-cell-${entry.name}-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: "white",
                  border: "none",
                  borderRadius: 12,
                  boxShadow: "0 4px 16px rgba(0,0,0,0.1)",
                  fontSize: 13,
                  fontWeight: 500,
                }}
              />
            </PieChart>
          </ResponsiveContainer>
          <div className="grid grid-cols-2 gap-3 mt-4">
            {departmentData.map((dept, index) => (
              <div key={`dept-legend-${dept.name}-${index}`} className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full" style={{ backgroundColor: dept.color }} />
                <div className="flex-1 min-w-0">
                  <p className="text-[11px] text-[#86868B] truncate">{dept.name}</p>
                  <p className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 600 }}>{dept.value}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* MO Workload */}
      <div className="bg-[#F5F5F7] rounded-3xl p-7 mb-6">
        <div className="mb-6">
          <h3 className="text-[18px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>MO Workload Distribution</h3>
          <p className="text-[13px] text-[#86868B] mt-1">Review activity by recruiting officer</p>
        </div>
        <ResponsiveContainer width="100%" height={280}>
          <BarChart data={workloadData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#E5E5EA" />
            <XAxis dataKey="mo" tick={{ fill: "#86868B", fontSize: 12, fontWeight: 500 }} />
            <YAxis tick={{ fill: "#86868B", fontSize: 12 }} />
            <Tooltip
              contentStyle={{
                backgroundColor: "white",
                border: "none",
                borderRadius: 12,
                boxShadow: "0 4px 16px rgba(0,0,0,0.1)",
                fontSize: 13,
                fontWeight: 500,
              }}
              cursor={{ fill: "rgba(0,113,227,0.05)" }}
            />
            <Legend
              wrapperStyle={{ fontSize: 12, fontWeight: 500 }}
              iconType="circle"
            />
            <Bar key="bar-pending" dataKey="pending" fill="#FF9F0A" radius={[8, 8, 0, 0]} name="Pending" />
            <Bar key="bar-reviewed" dataKey="reviewed" fill="#0071E3" radius={[8, 8, 0, 0]} name="Reviewed" />
            <Bar key="bar-hired" dataKey="hired" fill="#34C759" radius={[8, 8, 0, 0]} name="Hired" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Recent Activity */}
      <div className="bg-[#F5F5F7] rounded-3xl p-7">
        <div className="mb-5">
          <h3 className="text-[18px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Recent Activity</h3>
          <p className="text-[13px] text-[#86868B] mt-1">Latest recruitment actions across all departments</p>
        </div>
        <div className="space-y-2">
          {recentActivity.map((activity, index) => {
            const statusStyles = {
              accepted: { bg: "#E8F8F0", text: "#34C759", border: "#C8E6C9" },
              pending: { bg: "#FFF8E1", text: "#FF9F0A", border: "#FFE082" },
              interview: { bg: "#E3F2FD", text: "#0071E3", border: "#BBDEFB" },
              rejected: { bg: "#FFEBEE", text: "#FF3B30", border: "#FFCDD2" },
            };
            const style = statusStyles[activity.status as keyof typeof statusStyles];
            return (
              <div key={index} className="bg-white rounded-2xl p-5 flex items-center justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-1">
                    <p className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 600 }}>{activity.applicant}</p>
                    <div
                      className="px-2.5 py-1 rounded-full text-[10px] uppercase tracking-wider border"
                      style={{
                        backgroundColor: style.bg,
                        color: style.text,
                        borderColor: style.border,
                        fontWeight: 600,
                      }}
                    >
                      {activity.status}
                    </div>
                  </div>
                  <p className="text-[13px] text-[#86868B]">{activity.action} • {activity.job}</p>
                </div>
                <span className="text-[12px] text-[#AEAEB2] ml-4">{activity.time}</span>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}