package com.crowdin.client.reports;

import com.crowdin.client.core.http.exceptions.HttpBadRequestException;
import com.crowdin.client.core.model.*;
import com.crowdin.client.framework.RequestMock;
import com.crowdin.client.framework.TestClient;
import com.crowdin.client.reports.model.*;
import com.crowdin.client.reports.model.Currency;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Date;
import java.util.Calendar;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ReportsApiTest extends TestClient {

    private final Long projectId = 1L;
    private final Long reportSettingsTemplateId = 2L;
    private final String languageId = "ach";
    private final String engLanguageId = "uk";
    private final String postEditingCategory = "0-10";
    private final float fullTranslationRate = 0.1F;
    private final float proofreadRate = 0.12F;
    private final long fileId = 138L;
    private final long directoryId = 11L;
    private final long branchId = 18L;
    private final long labelId = 13L;

    private final String name = "my report template";
    private final String id = "50fb3506-4127-4ba8-8296-f97dc7e3e0c3";
    private final String link = "test.com";
    private final TimeZone tz = TimeZone.getTimeZone("GMT");
    private final Calendar calendar = GregorianCalendar.getInstance(tz);

    // -- REPORT ARCHIVE --//

    private final Long archiveId = 1L;
    private final String scopeType = "project";
    private final Long scopeId = 1L;
    private final Long userId = 1L;
    private final String archiveName = "my archive report";
    private final String webUrl = "https://crowdin.com/project/project-identifier/reports/archive/1";

    private final String exportId = "1";

    @Override
    public List<RequestMock> getMocks() {
        return Arrays.asList(
                RequestMock.build(this.url + "/projects/" + projectId + "/reports", HttpPost.METHOD_NAME, "api/reports/generateReport.json", "api/reports/reportGenerationStatus.json"),
                RequestMock.build(this.url + "/projects/" + projectId + "/reports", HttpPost.METHOD_NAME, "api/reports/generatePreTranslateAccuracyReport.json", "api/reports/preTranslateAccuracyReportStatus.json"),
                RequestMock.build(this.url + "/projects/" + projectId + "/reports/" + id, HttpGet.METHOD_NAME, "api/reports/reportGenerationStatus.json"),
                RequestMock.build(this.url + "/projects/" + projectId + "/reports/" + id + "/download", HttpGet.METHOD_NAME, "api/reports/downloadLink.json"),
                RequestMock.build(this.url + "/projects/" + projectId + "/reports/settings-templates", HttpGet.METHOD_NAME, "api/reports/listReportSettingsTemplate.json"),
                RequestMock.build(this.url + "/projects/" + projectId + "/reports/settings-templates", HttpPost.METHOD_NAME, "api/reports/addReportSettingsTemplate.json", "api/reports/reportSettingsTemplate.json"),
                RequestMock.build(this.url + "/projects/" + projectId + "/reports/settings-templates/" + reportSettingsTemplateId, HttpGet.METHOD_NAME, "api/reports/reportSettingsTemplate.json"),
                RequestMock.build(this.url + "/projects/" + projectId + "/reports/settings-templates/" + reportSettingsTemplateId, HttpPatch.METHOD_NAME, "api/reports/editReportSettingsTemplate.json", "api/reports/reportSettingsTemplate.json"),
                RequestMock.build(this.url + "/projects/" + projectId + "/settings-templates/" + reportSettingsTemplateId, HttpDelete.METHOD_NAME),
                RequestMock.build(this.url + "/users/" + userId + "/reports/archives/", HttpGet.METHOD_NAME, "api/reports/listReportArchives.json"),
                RequestMock.build(this.url + "/users/" + userId + "/reports/archives/" + archiveId, HttpGet.METHOD_NAME, "api/reports/reportArchive.json"),
                RequestMock.build(this.url + "/users/" + userId + "/reports/archives/" + archiveId, HttpDelete.METHOD_NAME),
                RequestMock.build(this.url + "/users/" + userId + "/reports/archives/" + archiveId + "/exports", HttpPost.METHOD_NAME, "api/reports/exportReportArchiveReques.json", "api/reports/reportGenerationStatus.json"),
                RequestMock.build(this.url + "/users/" + userId + "/reports/archives/" + archiveId + "/exports/" + exportId, HttpGet.METHOD_NAME, "api/reports/reportGenerationStatus.json"),
                RequestMock.build(this.url + "/users/" + userId + "/reports/archives" + archiveId + "/exports/" + exportId + "/download", HttpGet.METHOD_NAME, "api/reports/downloadLink.json"));
    }

    private ReportSettingsTemplate createSettingsTemplate() {
        ReportSettingsTemplate.RegularRate regular = new ReportSettingsTemplate.RegularRate();
        regular.setMode(ReportSettingsTemplate.Mode.NO_MATCH);
        regular.setValue(0.1);

        ReportSettingsTemplate.RegularRate innerRate = new ReportSettingsTemplate.RegularRate();
        innerRate.setMode(ReportSettingsTemplate.Mode.TM_MATCH);
        innerRate.setValue(0.1);

        ReportSettingsTemplate.IndividualRate individual = new ReportSettingsTemplate.IndividualRate();
        individual.setLanguageIds(Collections.singletonList("uk"));
        individual.setUserIds(Collections.singletonList(20));
        individual.setRates(Collections.singletonList(innerRate));

        List<ReportSettingsTemplate.RegularRate> regularRates = Collections.singletonList(regular);
        List<ReportSettingsTemplate.IndividualRate> individualRates = Collections.singletonList(individual);

        ReportSettingsTemplate.Config config = new ReportSettingsTemplate.Config();
        config.setRegularRates(regularRates);
        config.setIndividualRates(individualRates);

        ReportSettingsTemplate template = new ReportSettingsTemplate();
        template.setName(name);
        template.setCurrency(Currency.USD);
        template.setUnit(Unit.STRINGS);
        template.setConfig(config);
        template.setIsPublic(false);

        return template;
    }

    private Date getDate(int year, int month, int date, int hour, int minute, int second) {
        calendar.set(year, month, date, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @Test
    public void generateReportTest() {
        TimeZone.setDefault(tz);
        Date reportCreatedAt = getDate(2019, Calendar.SEPTEMBER, 23, 11, 26, 54);
        CostEstimationPostEditingGenerateReportRequest request = new CostEstimationPostEditingGenerateReportRequest();
        CostEstimationPostEditingGenerateReportRequest.GeneralSchema schema = new CostEstimationPostEditingGenerateReportRequest.GeneralSchema();
        schema.setUnit(Unit.STRINGS);
        schema.setCurrency(Currency.USD);
        schema.setLanguageId(languageId);
        schema.setFormat(ReportsFormat.XLSX);
        BaseRatesForm baseRates = new BaseRatesForm();
        baseRates.setFullTranslation(fullTranslationRate);
        baseRates.setProofread(proofreadRate);
        CostEstimationPostEditingGenerateReportRequest.IndividualRate individualRate = new CostEstimationPostEditingGenerateReportRequest.IndividualRate();
        individualRate.setLanguageIds(singletonList(engLanguageId));
        individualRate.setFullTranslation(fullTranslationRate);
        individualRate.setProofread(proofreadRate);
        CostEstimationPostEditingGenerateReportRequest.NetRateSchemes netRateSchemes = new CostEstimationPostEditingGenerateReportRequest.NetRateSchemes();
        Match match = new Match();
        match.setMatchType(MatchType.PERFECT);
        match.setPrice(fullTranslationRate);
        netRateSchemes.setTmMatch(singletonList(match));
        schema.setBaseRates(baseRates);
        schema.setIndividualRates(singletonList(individualRate));
        schema.setNetRateSchemes(netRateSchemes);
        schema.setCalculateInternalMatches(false);
        schema.setIncludePreTranslatedStrings(false);
        schema.setFileIds(singletonList(fileId));
        schema.setDirectoryIds(singletonList(directoryId));
        schema.setBranchIds(singletonList(branchId));
        schema.setLabelIds(singletonList(labelId));
        schema.setLabelIncludeType(LabelIncludeType.STRINGS_WITH_LABEL);
        request.setSchema(schema);
        ResponseObject<ReportStatus> reportStatusResponseObject = this.getReportsApi().generateReport(projectId, request);
        assertEquals(reportStatusResponseObject.getData().getIdentifier(), id);
        assertEquals(reportCreatedAt, reportStatusResponseObject.getData().getCreatedAt());
    }

    @Test
    public void testGeneratePreTranslateAccuracyReport() {
        TimeZone.setDefault(tz);
        Date reportCreatedAt = getDate(2019, Calendar.SEPTEMBER, 23, 11, 26, 54);

        PreTranslateAccuracyGenerateReportRequest request = new PreTranslateAccuracyGenerateReportRequest();
        PreTranslateAccuracyGenerateReportRequest.GeneralSchema schema = new PreTranslateAccuracyGenerateReportRequest.GeneralSchema();
        schema.setUnit(Unit.STRINGS);
        schema.setPostEditingCategories(singletonList(postEditingCategory));
        schema.setLanguageId(languageId);
        request.setSchema(schema);

        ResponseObject<ReportStatus> response = this.getReportsApi().generateReport(projectId, request);
        ReportStatus reportStatus = response.getData();
        ReportStatus.Attributes attributes = reportStatus.getAttributes();

        assertEquals(id, reportStatus.getIdentifier());
        assertEquals(ReportsFormat.XLSX, attributes.getFormat());
        assertEquals(request.getName(), attributes.getReportName());
        assertEquals(reportCreatedAt, reportStatus.getCreatedAt());
    }

    @Test
    public void checkReportGenerationStatusTest() {
        ResponseObject<ReportStatus> reportStatusResponseObject = this.getReportsApi().checkReportGenerationStatus(projectId, id);
        assertEquals(reportStatusResponseObject.getData().getIdentifier(), id);
    }

    @Test
    public void downloadReportTest() {
        ResponseObject<DownloadLink> downloadLinkResponseObject = this.getReportsApi().downloadReport(projectId, id);
        assertEquals(downloadLinkResponseObject.getData().getUrl(), link);
    }

    @Test
    public void listReportSettingsTemplateTest() {
        ResponseList<ReportSettingsTemplate> reportSettingsTemplateResponseList = this.getReportsApi().listReportSettingsTemplate(projectId, null, null);
        assertEquals(reportSettingsTemplateResponseList.getData().size(), 1);
        assertEquals(reportSettingsTemplateResponseList.getData().get(0).getData().getId(), projectId);
        assertEquals(reportSettingsTemplateResponseList.getData().get(0).getData().getName(), name);
    }

    @Test
    public void addReportSettingsTemplateTest() {
        ReportSettingsTemplate request = createSettingsTemplate();
        ResponseObject<ReportSettingsTemplate> reportSettingsTemplateResponseObject = this.getReportsApi().addReportSettingsTemplate(projectId, request);
        ReportSettingsTemplate response = reportSettingsTemplateResponseObject.getData();
        assertEquals(request.getName(), response.getName());
        assertEquals(request.getCurrency(), response.getCurrency());
        assertEquals(request.getUnit(), response.getUnit());
        assertEquals(request.getConfig(), response.getConfig());
        assertEquals(request.getIsPublic(), response.getIsPublic());
    }

    @Test
    public void getReportSettingsTemplateTest() {
        ResponseObject<ReportSettingsTemplate> responseObject = this.getReportsApi().getReportSettingsTemplate(projectId, reportSettingsTemplateId);
        assertEquals(responseObject.getData().getId(), projectId);
        assertEquals(responseObject.getData().getName(), name);
    }

    @Test
    public void editReportSettingsTemplateTest() {
        PatchRequest request = new PatchRequest();
        request.setOp(PatchOperation.REPLACE);
        request.setValue(name);
        request.setPath("name");
        ResponseObject<ReportSettingsTemplate> responseObject = this.getReportsApi().editReportSettingsTemplate(projectId, reportSettingsTemplateId, singletonList(request));
        assertEquals(responseObject.getData().getId(), projectId);
        assertEquals(responseObject.getData().getName(), name);
    }

    @Test
    public void deleteReportSettingsTemplateTest() {
        this.getReportsApi().deleteReportSettingsTemplate(projectId, reportSettingsTemplateId);
    }

    @Test
    public void getListReportArchivesTest() {
        ResponseList<ReportArchive> responseObject = this.getReportsApi().listReportArchives(userId, null, null, null, null);
        assertEquals(responseObject.getData().size(), 1);
        assertEquals(responseObject.getData().get(0).getData().getId(), archiveId);
        assertEquals(responseObject.getData().get(0).getData().getScopeType(), scopeType);
        assertEquals(responseObject.getData().get(0).getData().getScopeId(), scopeId);
        assertEquals(responseObject.getData().get(0).getData().getName(), archiveName);
        assertEquals(responseObject.getData().get(0).getData().getWebUrl(), webUrl);
    }

    @Test
    public void getReportArchivesTest() {
        ResponseObject<ReportArchive> reportArchive = this.getReportsApi().getReportArchive(userId, archiveId);
        assertEquals(reportArchive.getData().getWebUrl(), webUrl);
    }

    @Test
    public void deleteReportArchivesTest() {
        try {
            this.getReportsApi().deleteReportArchive(userId, archiveId);
            // If no exception is thrown, the delete operation is successful
        } catch (HttpBadRequestException e) {
            fail("Unexpected exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void exportReportArchiveTest() {
        ExportReportRequest request = new ExportReportRequest();
        request.setFormat(ReportsFormat.from("xlsx"));

        ResponseObject<GroupReportStatus> exportReportArchive = this.getReportsApi().exportReportArchive(userId, archiveId, request);
        assertEquals(exportReportArchive.getData().getAttributes().getFormat(), ReportsFormat.XLSX);
    }

    @Test
    public void checkReportArchiveExportStatusTest() {
        ResponseObject<GroupReportStatus> reportStatusResponseObject = this.getReportsApi().checkReportArchiveExportStatus(userId, archiveId, exportId);
        assertEquals(reportStatusResponseObject.getData().getIdentifier(), id);
    }

    @Test
    public void downloadReportArchiveTest() {
        ResponseObject<DownloadLink> downloadLinkResponseObject = this.getReportsApi().downloadReportArchive(userId, archiveId, exportId);
        assertEquals(downloadLinkResponseObject.getData().getUrl(), link);
    }
}
