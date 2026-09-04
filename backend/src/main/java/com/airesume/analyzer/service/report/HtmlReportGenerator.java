package com.airesume.analyzer.service.report;

import com.airesume.analyzer.dto.CanonicalReportModel;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class HtmlReportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateHtmlReport(CanonicalReportModel model) {
        if (model == null) {
            return "<div class='p-6 font-sans text-red-500'>Report data unavailable.</div>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='p-6 font-sans space-y-6 text-gray-800 dark:text-gray-200'>")

          // Document Header
          .append("<div class='p-6 rounded-2xl bg-gradient-to-r from-slate-900 via-indigo-950 to-primary-950 text-white text-center space-y-2'>")
          .append("<h1 class='text-2xl font-black tracking-tight uppercase text-primary-400'>EXECUTIVE ATS RESUME EVALUATION REPORT</h1>")
          .append("<p class='text-xs text-gray-300'>File: ").append(escape(model.getOriginalFileName()))
          .append(" | Candidate: ").append(escape(model.getCandidateName()))
          .append(" (").append(escape(model.getCandidateEmail())).append(")</p>")
          .append("<p class='text-[10px] text-gray-400'>Generated: ").append(model.getGeneratedDate() != null ? model.getGeneratedDate().format(DATE_FORMATTER) : "N/A").append("</p>")
          .append("</div>")

          // Score Overview Row
          .append("<div class='grid grid-cols-2 md:grid-cols-4 gap-4 text-center'>")
          .append(renderScoreCard("ATS Score", model.getAtsScore(), "/100", "primary"))
          .append(renderScoreCard("Grammar Index", model.getGrammarScore(), "/100", "purple"))
          .append(renderScoreCard("Target Job Match", model.getJobMatchPercentage(), "%", "emerald"))
          .append(renderScoreCard("Resume Strength", model.getOverallStrength(), "/100", "blue"))
          .append("</div>")

          // Section 1: Candidate Overview
          .append("<div class='p-5 rounded-2xl bg-gray-50 dark:bg-dark-hover space-y-2 border border-gray-200/60 dark:border-dark-border'>")
          .append("<h3 class='text-base font-bold text-gray-900 dark:text-white'>1. Candidate Overview & Summary</h3>")
          .append("<p class='text-xs text-gray-500 dark:text-gray-400'><strong>Location:</strong> ").append(escape(model.getCandidateLocation()))
          .append(" | <strong>Phone:</strong> ").append(escape(model.getCandidatePhone())).append("</p>")
          .append("<div class='text-xs sm:text-sm leading-relaxed text-gray-700 dark:text-gray-300 mt-2'>").append(escape(model.getSummary()).replace("\n", "<br/>")).append("</div>")
          .append("</div>")

          // Section 2: ATS Keyword Analysis
          .append("<div class='p-5 rounded-2xl bg-gray-50 dark:bg-dark-hover space-y-3 border border-gray-200/60 dark:border-dark-border'>")
          .append("<h3 class='text-base font-bold text-gray-900 dark:text-white'>2. ATS Keyword & Skill Analysis</h3>")
          .append("<div><strong class='text-xs text-gray-600 dark:text-gray-400 block mb-1'>Extracted Core Skills:</strong>")
          .append(renderBadges(model.getTopSkills(), "bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300"))
          .append("</div>")
          .append("<div><strong class='text-xs text-gray-600 dark:text-gray-400 block mb-1'>Missing Critical Keywords:</strong>")
          .append(renderBadges(model.getMissingKeywords(), "bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-300"))
          .append("</div>")
          .append("</div>")

          // Section 3: Grammar & Writing Quality
          .append("<div class='p-5 rounded-2xl bg-gray-50 dark:bg-dark-hover space-y-2 border border-gray-200/60 dark:border-dark-border'>")
          .append("<h3 class='text-base font-bold text-gray-900 dark:text-white'>3. Grammar & Writing Quality Details</h3>")
          .append("<p class='text-xs text-gray-600 dark:text-gray-400'>")
          .append("<strong>Weak Action Verbs Count:</strong> ").append(model.getGrammarWeakVerbCount() != null ? model.getGrammarWeakVerbCount() : "Not analyzed")
          .append(" | <strong>Passive Voice Sentences:</strong> ").append(model.getGrammarPassiveCount() != null ? model.getGrammarPassiveCount() : "Not analyzed")
          .append("</p>")
          .append("</div>")

          // Section 4: Target Job Match Details
          .append("<div class='p-5 rounded-2xl bg-gray-50 dark:bg-dark-hover space-y-3 border border-gray-200/60 dark:border-dark-border'>")
          .append("<h3 class='text-base font-bold text-gray-900 dark:text-white'>4. Target Job Match Analysis</h3>")
          .append("<p class='text-xs text-gray-600 dark:text-gray-400'><strong>Target Position:</strong> ").append(escape(model.getTargetJobTitle())).append("</p>")
          .append("<div><strong class='text-xs text-gray-600 dark:text-gray-400 block mb-1'>Matched Job Skills:</strong>")
          .append(renderBadges(model.getMatchedJobSkills(), "bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300"))
          .append("</div>")
          .append("<div><strong class='text-xs text-gray-600 dark:text-gray-400 block mb-1'>Missing Job Skills:</strong>")
          .append(renderBadges(model.getMissingJobSkills(), "bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300"))
          .append("</div>")
          .append("</div>")

          // Section 5: AI Suggestions & Rewrites
          .append("<div class='p-5 rounded-2xl bg-gray-50 dark:bg-dark-hover space-y-3 border border-gray-200/60 dark:border-dark-border'>")
          .append("<h3 class='text-base font-bold text-gray-900 dark:text-white'>5. AI Evidence-Grounded Recommendations</h3>")
          .append("<div class='space-y-2 text-xs sm:text-sm'>")
          .append("<div><strong class='text-primary-600 dark:text-primary-400 block'>Professional Summary Rewrite:</strong>")
          .append("<p class='text-gray-700 dark:text-gray-300 mt-0.5 font-medium'>").append(escape(model.getAiProfessionalSummary())).append("</p></div>")
          .append("<div><strong class='text-emerald-600 dark:text-emerald-400 block'>Quantified Experience Bullets:</strong>")
          .append("<p class='text-gray-700 dark:text-gray-300 mt-0.5 whitespace-pre-line'>").append(escape(model.getAiImprovedExperience())).append("</p></div>")
          .append("<div><strong class='text-indigo-600 dark:text-indigo-400 block'>Project Stack Enhancements:</strong>")
          .append("<p class='text-gray-700 dark:text-gray-300 mt-0.5 whitespace-pre-line'>").append(escape(model.getAiImprovedProjects())).append("</p></div>")
          .append("</div>")
          .append("</div>")

          .append("</div>");

        return sb.toString();
    }

    private String renderScoreCard(String title, Integer score, String unit, String theme) {
        String valStr = score != null ? (score + unit) : "Not analyzed";
        return "<div class='p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover border border-gray-200/60 dark:border-dark-border space-y-1'>" +
               "<span class='text-xl font-black text-" + theme + "-600 dark:text-" + theme + "-400'>" + escape(valStr) + "</span>" +
               "<p class='text-[10px] font-bold text-gray-500 uppercase'>" + escape(title) + "</p>" +
               "</div>";
    }

    private String renderBadges(List<String> items, String cssClass) {
        if (items == null || items.isEmpty()) {
            return "<span class='text-xs text-gray-400 italic'>Not analyzed / None</span>";
        }
        StringBuilder sb = new StringBuilder("<div class='flex flex-wrap gap-1.5 mt-1'>");
        for (String item : items) {
            sb.append("<span class='px-2.5 py-1 rounded-lg text-xs font-semibold ").append(cssClass).append("'>")
              .append(escape(item)).append("</span>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String escape(String text) {
        return text != null ? HtmlUtils.htmlEscape(text) : "";
    }
}
