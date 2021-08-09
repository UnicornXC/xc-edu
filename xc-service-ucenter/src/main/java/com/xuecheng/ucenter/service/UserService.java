package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserService {

    @Autowired
    private XcUserRepository xcUserRepository;

    @Autowired
    private XcCompanyUserRepository xcCompanyUserRepository;

    @Autowired
    private XcMenuMapper xcMenuMapper;

    /**
     * 根据用户名查找用户的扩展信息
     * @param username
     * @return
     */
    public XcUserExt getUserExt(String username) {
        XcUser xcUser = this.findXcUserByUsername(username);
        if (xcUser == null) {
            return null;
        }
        String userId = xcUser.getId();
        // 查找扩展公司信息
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(userId);
        // 查询用户的权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);
        String companyId = null;
        if (xcCompanyUser != null) {
            companyId = xcCompanyUser.getCompanyId();
        }
        XcUserExt ext = new XcUserExt();
        BeanUtils.copyProperties(xcUser, ext);
        ext.setCompanyId(companyId);
        ext.setPermissions(xcMenus);
        return ext;
    }

    /**
     * 根据用户名称查找用户信息
     * @param username
     * @return
     */
    public XcUser findXcUserByUsername(String username) {
        return xcUserRepository.findByUsername(username);

    }
}
