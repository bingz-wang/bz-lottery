package com.lottery.user.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.user.domain.entity.SysMenu;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("""
            select distinct m.*
            from lottery_platform.sys_menu m
            inner join lottery_platform.role_menu_rel rm on rm.menu_id = m.id
            inner join lottery_platform.user_role_rel ur on ur.role_id = rm.role_id
            inner join lottery_platform.sys_role r on r.id = ur.role_id
            where ur.user_id = #{userId}
              and m.status = 1
              and m.visible = true
              and m.deleted = false
              and r.status = 1
              and r.deleted = false
            order by m.sort asc, m.id asc
            """)
    List<SysMenu> findMenusByUserId(Long userId);
}
