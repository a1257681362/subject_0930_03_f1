package com.evops.museum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evops.museum.entity.Artifact;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArtifactMapper extends BaseMapper<Artifact> {
}
