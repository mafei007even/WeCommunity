package com.aatroxc.wecommunity.controller;

import com.aatroxc.wecommunity.service.DataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author mafei007
 * @date 2020/5/17 23:13
 */

@Controller
public class DataController {

	private final DataService dataService;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public DataController(DataService dataService) {
		this.dataService = dataService;
	}


	@RequestMapping(path = "data", method = {RequestMethod.GET, RequestMethod.POST})
	public String getDataPage() {
		return "site/admin/data";
	}

	/**
	 * 统计网站 uv
	 *
	 * @param start
	 * @param end
	 * @param model
	 * @return
	 */
	@PostMapping("data/uv")
	public String getUV(@RequestParam LocalDate start, @RequestParam LocalDate end, Model model) {
		// 开始日期大于截至日期
		if (start.isAfter(end)) {
			model.addAttribute("uvResult", "开始日期不能大于截至日期");
			return "site/admin/data";
		}

		long uv = dataService.calculateUV(start, end);
		model.addAttribute("uvResult", uv);
		model.addAttribute("uvStartDate", start.format(formatter));
		model.addAttribute("uvEndDate", end.format(formatter));
		return "site/admin/data";
	}

	/**
	 * 统计 dau
	 *
	 * @param start
	 * @param end
	 * @param model
	 * @return
	 */
	@PostMapping("data/dau")
	public String getDAU(@RequestParam LocalDate start, @RequestParam LocalDate end, Model model) {
		// 开始日期大于截至日期
		if (start.isAfter(end)) {
			model.addAttribute("dauResult", "开始日期不能大于截至日期");
			return "site/admin/data";
		}

		long uv = dataService.calculateDAU(start, end);
		model.addAttribute("dauResult", uv);
		model.addAttribute("dauStartDate", start.format(formatter));
		model.addAttribute("dauEndDate", end.format(formatter));
		return "site/admin/data";
	}

}
