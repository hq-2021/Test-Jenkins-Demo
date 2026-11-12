package com.example.demo.controller;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/test2")
@RestController
public class ExportController {


    @GetMapping("/export/chart")
    public void exportChart(HttpServletResponse response) throws IOException {
        // 1. 读取模板（放在 resources/templates/chart_template.xlsx）
        InputStream templateStream = getClass().getResourceAsStream("/templates/chart_template.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook(templateStream)) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            // 2. 准备数据（X轴日期，Y轴数值）
            List<String> dates = Arrays.asList("1日", "2日", "3日", "4日", "5日");
            List<Double> values = Arrays.asList(10.5, 18.2, 15.0, 25.8, 30.1);

            // 3. 填充数据到模板预留位置（假设预留了 A2:A6 和 B2:B6）
            for (int i = 0; i < dates.size(); i++) {
                sheet.getRow(i + 1).getCell(0).setCellValue(dates.get(i)); // X轴
                sheet.getRow(i + 1).getCell(1).setCellValue(values.get(i)); // Y轴
            }

            // 4. 刷新图表数据范围（如果模板图表数据源指定的是固定区域，填充后自动更新）

            // 5. 导出
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=chart_export.xlsx");
            workbook.write(response.getOutputStream());
        }
    }
}
