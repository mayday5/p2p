package com.esoft.archer.link.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esoft.archer.common.controller.EntityHome;
import com.esoft.archer.link.LinkConstants;
import com.esoft.archer.link.model.Link;
import com.esoft.archer.link.model.LinkType;
import com.esoft.archer.operationlog.service.OperationLogService;
import com.esoft.archer.system.controller.LoginUserInfo;
import com.esoft.core.annotations.Logger;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.jsf.util.FacesUtil;
import com.esoft.core.util.StringManager;
import com.esoft.jdp2p.loan.model.OperationLog;

@Component
@Scope(ScopeType.VIEW)
public class LinkTypeHome extends EntityHome<LinkType> {
	@Logger
	static Log log;
	@Resource
	private HibernateTemplate ht;
	
	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private OperationLogService operationLogService;
	@Autowired
	private HttpServletRequest request;
	private static final StringManager sm = StringManager
			.getManager(LinkConstants.Package);

	public LinkTypeHome() {
		setUpdateView(FacesUtil.redirect(LinkConstants.View.LINK_TYPE_LIST));
	}

	public List<LinkType> getAllLinkType() {
		return super.findAll();
	}

	@Override
	//FIXME: 查询方式换成HQL 。
	@Transactional(readOnly=false)
	public String delete() {
		String typeId = this.getInstance().getId();
		DetachedCriteria criteria = DetachedCriteria.forClass(Link.class);
		criteria.add(Restrictions.eq("type.id", typeId));
		ht.setCacheQueries(true);
		List<Link> links = ht.findByCriteria(criteria);
		if (links.size() > 0) {
			FacesUtil.addWarnMessage(sm.getString("canNotDeleteLinkType"));
			if (log.isInfoEnabled()) {
				log.info(sm.getString("log.info.deleteLinkTypeUnsuccessful",
						FacesUtil
						.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
			}
			return null;
		}
		//后台操作日志
		LinkType linkType = getBaseService().get(LinkType.class,getId());
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(linkType.getId());
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		operationLog.setOperationName(linkType.getName());
		operationLog.setOperationAction("链接分类管理-删除");
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		return super.delete();
	}
	
	@Override
	@Transactional(readOnly=false)
	public String save() {
		LinkType linkType = getInstance();
		//后台操作日志
		OperationLog operationLog = new OperationLog();
		operationLog.setOperationId(linkType.getId());
		LinkType lt = getBaseService().get(LinkType.class,linkType.getId());
		operationLog.setOperationName(linkType.getName());
		operationLog.setOperationAction("链接分类管理-修改");
		operationLog.setOperationIp(FacesUtil.getRequestIp(request));
		if(lt==null){
			operationLog.setOperationAction("链接分类管理-新建");
		}
		operationLog.setOperationDate(new Date());
		operationLog.setOperationType("系统设置");
		operationLog.setOperationUser(loginUser.getLoginUserId());
		operationLogService.save(operationLog);
		return super.save();
	}
}
