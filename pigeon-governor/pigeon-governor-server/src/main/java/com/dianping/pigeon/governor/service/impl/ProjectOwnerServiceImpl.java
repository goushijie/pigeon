package com.dianping.pigeon.governor.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.pigeon.governor.dao.ProjectOwnerMapper;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.ProjectOwner;
import com.dianping.pigeon.governor.model.ProjectOwnerExample;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.UserService;
import com.dianping.pigeon.governor.util.CmdbUtils;

@Service
public class ProjectOwnerServiceImpl implements ProjectOwnerService {

	@Autowired
	private ProjectOwnerMapper projectOwnerMapper;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ProjectService projectService;
	
	@Override
	public boolean isProjectOwner(String dpaccount, String projectName) {
		Project project = projectService.findProject(projectName);
		
		return isProjectOwner(dpaccount, project);
	}

	@Override
	public boolean isProjectOwner(String dpaccount, Project project) {
		
		if(project != null) {
			ProjectOwnerExample example = new ProjectOwnerExample();
			example.createCriteria().andProjectidEqualTo(project.getId());
			List<ProjectOwner> projectOwners = projectOwnerMapper.selectByExample(example);
			
			if(projectOwners != null && projectOwners.size() > 0) {
				User user = userService.retrieveByDpaccount(dpaccount);
				
				if(user != null){
					Integer userid = user.getId();
					
					for(ProjectOwner projectOwner : projectOwners) {
						
						if(userid.equals(projectOwner.getUserid())){
							
							return true;
						}
					}
				}
				
			}
			
		}
		
		return false;
	}

	@Override
	public void createDefaultOwner(String email) {
		Project project = projectService.retrieveByEmail(email);
		
		if(project != null) {
			String dpAccount = email.split("@")[0];
			User user = userService.retrieveByDpaccount(dpAccount);
			
			if(user != null) {
				ProjectOwner projectOwner = new ProjectOwner();
				projectOwner.setProjectid(project.getId());
				projectOwner.setUserid(user.getId());
				projectOwner.setCreatetime(new Date());
				projectOwnerMapper.insertSelective(projectOwner);
			}
		}
	}

	
}