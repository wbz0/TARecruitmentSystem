import { Link } from "react-router";
import { ArrowLeft, Target, BookOpen, TrendingUp, ExternalLink, CheckCircle2 } from "lucide-react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from "recharts";

const skillGapData = [
  { skill: "TypeScript", gap: 5, priority: "High" },
  { skill: "Node.js", gap: 5, priority: "High" },
  { skill: "GraphQL", gap: 15, priority: "Medium" },
  { skill: "Docker", gap: 20, priority: "Low" },
];

const learningResources = [
  {
    skill: "TypeScript",
    resources: [
      { title: "TypeScript Handbook", url: "#", type: "Documentation" },
      { title: "TypeScript Deep Dive", url: "#", type: "Book" },
      { title: "TypeScript Course", url: "#", type: "Video" },
    ],
  },
  {
    skill: "Node.js",
    resources: [
      { title: "Node.js Official Docs", url: "#", type: "Documentation" },
      { title: "Node.js Design Patterns", url: "#", type: "Book" },
      { title: "Node.js Masterclass", url: "#", type: "Video" },
    ],
  },
];

const improvementPlan = [
  { week: "Week 1-2", focus: "TypeScript Fundamentals", status: "current" },
  { week: "Week 3-4", focus: "Advanced TypeScript", status: "upcoming" },
  { week: "Week 5-6", focus: "Node.js Basics", status: "upcoming" },
  { week: "Week 7-8", focus: "Node.js + TypeScript Integration", status: "upcoming" },
];

export function MissingSkills() {
  return (
    <div className="p-8 max-w-5xl mx-auto">
      {/* Back */}
      <Link
        to="/skill-match"
        className="inline-flex items-center gap-1.5 text-[13px] text-[#0071E3] hover:underline mb-6"
        style={{ fontWeight: 500 }}
      >
        <ArrowLeft className="w-3.5 h-3.5" strokeWidth={2} />
        Back to Skill Match
      </Link>

      <div className="mb-7">
        <h2 className="text-[26px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Skill Improvement Plan</h2>
        <p className="text-[15px] text-[#86868B] mt-1">AI-generated recommendations to close your skill gaps.</p>
      </div>

      {/* Summary Card */}
      <div className="bg-gradient-to-br from-[#FF9F0A] to-[#FF8C00] rounded-3xl p-8 mb-6 text-white shadow-[0_8px_24px_rgba(255,159,10,0.25)]">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <Target className="w-5 h-5" strokeWidth={2} />
              <p className="text-[13px] opacity-90 uppercase tracking-widest" style={{ fontWeight: 500 }}>Skills to Improve</p>
            </div>
            <h3 className="text-[40px] tracking-tight" style={{ fontWeight: 700 }}>{skillGapData.length}</h3>
            <p className="text-[15px] mt-2 opacity-95" style={{ fontWeight: 500 }}>
              Focus on these skills to reach 95%+ match score for top positions
            </p>
          </div>
          <div className="flex flex-col gap-2">
            <div className="bg-white/20 backdrop-blur-sm rounded-xl px-4 py-2 text-center">
              <div className="text-[11px] opacity-80 uppercase tracking-wide" style={{ fontWeight: 500 }}>Avg Gap</div>
              <div className="text-[20px]" style={{ fontWeight: 700 }}>11%</div>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl px-4 py-2 text-center">
              <div className="text-[11px] opacity-80 uppercase tracking-wide" style={{ fontWeight: 500 }}>Time</div>
              <div className="text-[20px]" style={{ fontWeight: 700 }}>8w</div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        {/* Skill Gap Chart */}
        <div className="bg-[#F5F5F7] rounded-3xl p-7">
          <div className="mb-5">
            <h3 className="text-[18px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Skill Gaps</h3>
            <p className="text-[13px] text-[#86868B] mt-1">Points needed to meet requirements</p>
          </div>
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={skillGapData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#E5E5EA" />
              <XAxis dataKey="skill" tick={{ fill: "#86868B", fontSize: 12, fontWeight: 500 }} />
              <YAxis tick={{ fill: "#86868B", fontSize: 12 }} label={{ value: "Gap %", angle: -90, position: "insideLeft", fill: "#86868B", fontSize: 12 }} />
              <Tooltip
                contentStyle={{
                  backgroundColor: "white",
                  border: "none",
                  borderRadius: 12,
                  boxShadow: "0 4px 16px rgba(0,0,0,0.1)",
                  fontSize: 13,
                  fontWeight: 500,
                }}
                cursor={{ fill: "rgba(255,159,10,0.05)" }}
              />
              <Bar dataKey="gap" radius={[8, 8, 0, 0]}>
                {skillGapData.map((entry, index) => {
                  const colors = { High: "#FF3B30", Medium: "#FF9F0A", Low: "#34C759" };
                  return <Cell key={`gap-cell-${entry.skill}-${index}`} fill={colors[entry.priority as keyof typeof colors]} />;
                })}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Priority List */}
        <div className="bg-[#F5F5F7] rounded-3xl p-7">
          <div className="mb-5">
            <h3 className="text-[18px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Priority Ranking</h3>
            <p className="text-[13px] text-[#86868B] mt-1">Skills ordered by impact on match score</p>
          </div>
          <div className="space-y-3">
            {skillGapData.map((item, index) => {
              const priorityColors = {
                High: { bg: "#FFEBEE", text: "#FF3B30", border: "#FFCDD2" },
                Medium: { bg: "#FFF8E1", text: "#FF9F0A", border: "#FFE082" },
                Low: { bg: "#E8F5E9", text: "#34C759", border: "#C8E6C9" },
              };
              const color = priorityColors[item.priority as keyof typeof priorityColors];
              return (
                <div key={item.skill} className="bg-white rounded-2xl p-5 flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="w-8 h-8 bg-[#F5F5F7] rounded-full flex items-center justify-center text-[13px] text-[#1D1D1F]" style={{ fontWeight: 700 }}>
                      {index + 1}
                    </div>
                    <div>
                      <p className="text-[15px] text-[#1D1D1F]" style={{ fontWeight: 600 }}>{item.skill}</p>
                      <p className="text-[12px] text-[#86868B] mt-0.5">Gap: {item.gap} points</p>
                    </div>
                  </div>
                  <div
                    className="px-3 py-1.5 rounded-full text-[11px] uppercase tracking-wider border"
                    style={{
                      backgroundColor: color.bg,
                      color: color.text,
                      borderColor: color.border,
                      fontWeight: 600,
                    }}
                  >
                    {item.priority}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Learning Resources */}
      <div className="bg-[#F5F5F7] rounded-3xl p-7 mb-6">
        <div className="mb-6">
          <div className="flex items-center gap-2 mb-1">
            <BookOpen className="w-5 h-5 text-[#0071E3]" strokeWidth={1.75} />
            <h3 className="text-[18px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Learning Resources</h3>
          </div>
          <p className="text-[13px] text-[#86868B] mt-1">AI-curated resources to help you improve</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {learningResources.map((skillResource) => (
            <div key={skillResource.skill} className="bg-white rounded-2xl p-5">
              <div className="flex items-center gap-2 mb-4">
                <div className="w-8 h-8 bg-[#0071E3]/10 rounded-lg flex items-center justify-center">
                  <TrendingUp className="w-4 h-4 text-[#0071E3]" strokeWidth={2} />
                </div>
                <h4 className="text-[15px] text-[#1D1D1F]" style={{ fontWeight: 600 }}>{skillResource.skill}</h4>
              </div>
              <div className="space-y-2.5">
                {skillResource.resources.map((resource, idx) => (
                  <a
                    key={idx}
                    href={resource.url}
                    className="flex items-center justify-between p-3 bg-[#F5F5F7] rounded-xl hover:bg-[#E8E8ED] transition-colors group"
                  >
                    <div className="flex-1">
                      <p className="text-[13px] text-[#1D1D1F] group-hover:text-[#0071E3] transition-colors" style={{ fontWeight: 500 }}>
                        {resource.title}
                      </p>
                      <p className="text-[11px] text-[#86868B] mt-0.5">{resource.type}</p>
                    </div>
                    <ExternalLink className="w-3.5 h-3.5 text-[#C7C7CC] group-hover:text-[#0071E3] transition-colors" strokeWidth={2} />
                  </a>
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 8-Week Improvement Plan */}
      <div className="bg-[#F5F5F7] rounded-3xl p-7">
        <div className="mb-6">
          <div className="flex items-center gap-2 mb-1">
            <Target className="w-5 h-5 text-[#34C759]" strokeWidth={1.75} />
            <h3 className="text-[18px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>8-Week Roadmap</h3>
          </div>
          <p className="text-[13px] text-[#86868B] mt-1">Structured learning path to reach your goals</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {improvementPlan.map((phase, index) => {
            const isCurrent = phase.status === "current";
            return (
              <div
                key={index}
                className={`rounded-2xl p-5 border-2 transition-all ${
                  isCurrent
                    ? "bg-[#E8F8F0] border-[#34C759]"
                    : "bg-white border-[#E5E5EA]"
                }`}
              >
                <div className="flex items-center gap-2 mb-3">
                  {isCurrent ? (
                    <div className="w-6 h-6 bg-[#34C759] rounded-full flex items-center justify-center">
                      <div className="w-2 h-2 bg-white rounded-full animate-pulse" />
                    </div>
                  ) : (
                    <div className="w-6 h-6 bg-[#F5F5F7] rounded-full border-2 border-[#E5E5EA]" />
                  )}
                  <span className={`text-[11px] uppercase tracking-wider ${isCurrent ? "text-[#34C759]" : "text-[#86868B]"}`} style={{ fontWeight: 600 }}>
                    {phase.week}
                  </span>
                </div>
                <p className={`text-[14px] ${isCurrent ? "text-[#1D1D1F]" : "text-[#86868B]"}`} style={{ fontWeight: isCurrent ? 600 : 500 }}>
                  {phase.focus}
                </p>
                {isCurrent && (
                  <div className="mt-3 text-[11px] text-[#34C759] uppercase tracking-wide" style={{ fontWeight: 600 }}>
                    → In Progress
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}