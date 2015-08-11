package com.nardix.backup.rs;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class BackupController {
	
	@RequestMapping("/backup")
	public String backup() {
		return "performing backup....";
		
	}

}
