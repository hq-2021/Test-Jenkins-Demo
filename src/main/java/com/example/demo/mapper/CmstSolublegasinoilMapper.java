package com.example.demo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.CmstSolublegasinoil;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CmstSolublegasinoilMapper extends BaseMapper<CmstSolublegasinoil> {

    List<CmstSolublegasinoil> getCmstSolublegasinoilList();


}




