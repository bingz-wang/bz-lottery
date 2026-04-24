package com.lottery.user.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lottery.user.domain.entity.SysRole;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("""
            select r.*
            from lottery_platform.sys_role r
            inner join lottery_platform.user_role_rel ur on ur.role_id = r.id
            where ur.user_id = #{userId}
              and r.status = 1
              and r.deleted = false
            order by r.sort asc, r.id asc
            """)
    List<SysRole> findRolesByUserId(Long userId);
}
