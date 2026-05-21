import { Link } from "react-router";
import { ArrowLeft, TrendingUp, CheckCircle2, AlertCircle } from "lucide-react";
import { RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Cell } from "recharts";

const skillRadarData = [
  { skill: "React", current: 90, required: 85 },
  { skill: "TypeScript", current: 75, required: 80 },
  { skill: "Node.js", current: 65, required: 70 },
  { skill: "Database", current: 80, required: 75 },
  { skill: "UI/UX", current: 85, required: 70 },
];

const jobMatchData = [
  { job: "CS 101 TA", match: 92, color: "#34C759" },
  { job: "Web Dev TA", match: 88, color: "#0071E3" },
  { job: "AI Lab TA", match: 76, color: "#FF9F0A" },
  { job: "Data TA", match: 68, color: "#FF3B30" },
];

export function SkillMatch() {
  const overallMatch = 85;

  return (
    <div className="p-8 max-w-5xl mx-auto">
      {/* Back */}
      <Link
        to="/"
        className="inline-flex items-center gap-1.5 text-[13px] text-[#0071E3] hover:underline mb-6"
        style={{ fontWeight: 500 }}
      >
        <ArrowLeft className="w-3.5 h-3.5" strokeWidth={2} />
        Back to Jobs
      </Link>

      <div className="mb-7">
        <h2 className="text-[26px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>AI Skill Match Analysis</h2>
        <p className="text-[15px] text-[#86868B] mt-1">See how your skills align with job requirements.</p>
      </div>

      {/* Overall Match Score */}
      <div className="bg-gradient-to-br from-[#0071E3] to-[#0077ED] rounded-3xl p-8 mb-6 text-white shadow-[0_8px_24px_rgba(0,113,227,0.25)]">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-[13px] opacity-90 uppercase tracking-widest" style={{ fontWeight: 500 }}>Overall Match Score</p>
            <h3 className="text-[56px] tracking-tight mt-2" style={{ fontWeight: 700 }}>{overallMatch}%</h3>
            <div className="flex items-center gap-2 mt-3">
              <TrendingUp className="w-4 h-4" strokeWidth={2} />
              <span className="text-[14px]" style={{ fontWeight: 500 }}>Strong match for this position</span>
            </div>
          </div>
          <div className="w-32 h-32 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center border-4 border-white/30">
            <div className="text-center">
              <div className="text-[32px]" style={{ fontWeight: 700 }}>{overallMatch}</div>
              <div className="text-[11px] opacity-80 uppercase tracking-wide" style={{ fontWeight: 500 }}>Score</div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Radar Chart - Skill Comparison */}
        <div className="bg-[#F5F5F7] rounded-3xl p-7">
          <div className="mb-5">
            <h3 className="text-[18px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Skill Radar</h3>
            <p className="text-[13px] text-[#86868B] mt-1">Your skills vs. job requirements</p>
          </div>
          <ResponsiveContainer width="100%" height={280}>
            <RadarChart data={skillRadarData}>
              <PolarGrid stroke="#E5E5EA" strokeWidth={1} />
              <PolarAngleAxis dataKey="skill" tick={{ fill: "#86868B", fontSize: 12, fontWeight: 500 }} />
              <PolarRadiusAxis angle={90} domain={[0, 100]} tick={{ fill: "#86868B", fontSize: 11 }} />
              <Radar 
                name="Your Skills" 
                dataKey="current" 
                stroke="#0071E3" 
                fill="#0071E3" 
                fillOpacity={0.3} 
                strokeWidth={2} 
                animationId="current"
                dot={false}
              />
              <Radar 
                name="Required" 
                dataKey="required" 
                stroke="#34C759" 
                fill="#34C759" 
                fillOpacity={0.15} 
                strokeWidth={2} 
                strokeDasharray="5 5" 
                animationId="required"
                dot={false}
              />
            </RadarChart>
          </ResponsiveContainer>
          <div className="flex items-center justify-center gap-6 mt-4">
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 bg-[#0071E3] rounded-full" />
              <span className="text-[12px] text-[#86868B]" style={{ fontWeight: 500 }}>Your Skills</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 bg-[#34C759] rounded-full opacity-50" />
              <span className="text-[12px] text-[#86868B]" style={{ fontWeight: 500 }}>Required</span>
            </div>
          </div>
        </div>

        {/* Skill Details */}
        <div className="bg-[#F5F5F7] rounded-3xl p-7">
          <div className="mb-5">
            <h3 className="text-[18px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Skill Breakdown</h3>
            <p className="text-[13px] text-[#86868B] mt-1">Detailed comparison by skill area</p>
          </div>
          <div className="space-y-4">
            {skillRadarData.map((skill) => {
              const isStrong = skill.current >= skill.required;
              const diff = skill.current - skill.required;
              return (
                <div key={skill.skill} className="bg-white rounded-2xl p-4">
                  <div className="flex items-center justify-between mb-2.5">
                    <div className="flex items-center gap-2">
                      {isStrong ? (
                        <CheckCircle2 className="w-4 h-4 text-[#34C759]" strokeWidth={2} />
                      ) : (
                        <AlertCircle className="w-4 h-4 text-[#FF9F0A]" strokeWidth={2} />
                      )}
                      <span className="text-[14px] text-[#1D1D1F]" style={{ fontWeight: 600 }}>{skill.skill}</span>
                    </div>
                    <div className="text-right">
                      <span className="text-[13px] text-[#1D1D1F]" style={{ fontWeight: 600 }}>{skill.current}%</span>
                      <span className="text-[12px] text-[#86868B]"> / {skill.required}%</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <div className="flex-1 h-2 bg-[#F5F5F7] rounded-full overflow-hidden">
                      <div
                        className="h-full rounded-full transition-all"
                        style={{
                          width: `${(skill.current / skill.required) * 100}%`,
                          backgroundColor: isStrong ? "#34C759" : "#FF9F0A",
                        }}
                      />
                    </div>
                    <span className={`text-[11px] ${isStrong ? "text-[#34C759]" : "text-[#FF9F0A]"}`} style={{ fontWeight: 600 }}>
                      {isStrong ? `+${diff}` : diff}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Job Match Rankings */}
      <div className="bg-[#F5F5F7] rounded-3xl p-7 mt-6">
        <div className="mb-6">
          <h3 className="text-[18px] text-[#1D1D1F] tracking-tight" style={{ fontWeight: 600 }}>Recommended Positions</h3>
          <p className="text-[13px] text-[#86868B] mt-1">TA positions ranked by your skill match</p>
        </div>
        <ResponsiveContainer width="100%" height={280}>
          <BarChart data={jobMatchData} layout="vertical" margin={{ left: 10, right: 30 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#E5E5EA" horizontal={false} />
            <XAxis type="number" domain={[0, 100]} tick={{ fill: "#86868B", fontSize: 12 }} />
            <YAxis dataKey="job" type="category" tick={{ fill: "#1D1D1F", fontSize: 13, fontWeight: 500 }} width={100} />
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
            <Bar dataKey="match" radius={[0, 8, 8, 0]}>
              {jobMatchData.map((entry, index) => (
                <Cell key={`job-cell-${entry.job}-${index}`} fill={entry.color} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Action button */}
      <div className="mt-6 flex justify-center">
        <Link
          to="/missing-skills"
          className="px-6 py-3 bg-[#0071E3] text-white rounded-full text-[14px] hover:bg-[#0077ED] transition-all active:scale-95 shadow-[0_2px_12px_rgba(0,113,227,0.28)]"
          style={{ fontWeight: 500 }}
        >
          View Skill Improvement Plan
        </Link>
      </div>
    </div>
  );
}