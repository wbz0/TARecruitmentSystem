import { createBrowserRouter } from "react-router";
import { TALayout } from "./components/TALayout";
import { MOLayout } from "./components/MOLayout";
import { AdminLayout } from "./components/AdminLayout";
import { TAJobsDashboard } from "./components/TAJobsDashboard";
import { JobDetail } from "./components/JobDetail";
import { ApplicationForm } from "./components/ApplicationForm";
import { ApplySuccess } from "./components/ApplySuccess";
import { ApplicationStatus } from "./components/ApplicationStatus";
import { MOApplicantsReview } from "./components/MOApplicantsReview";
import { MOPostJob } from "./components/MOPostJob";
import { MOApplicantDetail } from "./components/MOApplicantDetail";
import { TAProfile } from "./components/TAProfile";
import { MOOverview } from "./components/MOOverview";
import { MOSettings } from "./components/MOSettings";
import { Login } from "./components/Login";
import { Register } from "./components/Register";
import { SkillMatch } from "./components/SkillMatch";
import { MissingSkills } from "./components/MissingSkills";
import { AdminDashboard } from "./components/AdminDashboard";
import { AdminUsers } from "./components/AdminUsers";
import { AdminJobs } from "./components/AdminJobs";
import { AdminSettings } from "./components/AdminSettings";

export const router = createBrowserRouter([
  {
    path: "/login",
    Component: Login,
  },
  {
    path: "/register",
    Component: Register,
  },
  {
    path: "/",
    Component: TALayout,
    children: [
      { index: true, Component: TAJobsDashboard },
      { path: "job/:id", Component: JobDetail },
      { path: "job/:id/apply", Component: ApplicationForm },
      { path: "apply-success", Component: ApplySuccess },
      { path: "status", Component: ApplicationStatus },
      { path: "profile", Component: TAProfile },
      { path: "skill-match", Component: SkillMatch },
      { path: "missing-skills", Component: MissingSkills },
    ],
  },
  {
    path: "/mo",
    Component: MOLayout,
    children: [
      { index: true, Component: MOApplicantsReview },
      { path: "post-job", Component: MOPostJob },
      { path: "applicant/:id", Component: MOApplicantDetail },
      { path: "overview", Component: MOOverview },
      { path: "settings", Component: MOSettings },
    ],
  },
  {
    path: "/admin",
    Component: AdminLayout,
    children: [
      { index: true, Component: AdminDashboard },
      { path: "users", Component: AdminUsers },
      { path: "jobs", Component: AdminJobs },
      { path: "settings", Component: AdminSettings },
    ],
  },
]);