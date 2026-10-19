package com.clfw.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clfw.common.core.PageQuery;
import com.clfw.common.core.PageResult;
import com.clfw.common.core.R;
import com.clfw.common.log.BusinessType;
import com.clfw.framework.annotation.OperLog;
import com.clfw.system.domain.SysDictData;
import com.clfw.system.domain.SysDictType;
import com.clfw.system.service.SysDictDataService;
import com.clfw.system.service.SysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据字典：字典类型 + 字典数据。
 */
@Tag(name = "系统管理-数据字典")
@RestController
@RequestMapping("/system/dict")
public class SysDictController {

    private final SysDictTypeService dictTypeService;
    private final SysDictDataService dictDataService;

    public SysDictController(SysDictTypeService dictTypeService, SysDictDataService dictDataService) {
        this.dictTypeService = dictTypeService;
        this.dictDataService = dictDataService;
    }

    /* ---------- 字典类型 ---------- */

    @Operation(summary = "分页查询字典类型")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/type/page")
    public R<PageResult<SysDictType>> typePage(PageQuery query) {
        Page<SysDictType> page = dictTypeService.page(new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<SysDictType>().orderByDesc(SysDictType::getCreateTime));
        return R.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @Operation(summary = "新增字典类型")
    @SaCheckPermission("system:dict:add")
    @OperLog(module = "字典管理", type = BusinessType.INSERT)
    @PostMapping("/type")
    public R<Void> addType(@Valid @RequestBody SysDictType type) {
        dictTypeService.save(type);
        return R.ok();
    }

    @Operation(summary = "修改字典类型")
    @SaCheckPermission("system:dict:edit")
    @OperLog(module = "字典管理", type = BusinessType.UPDATE)
    @PutMapping("/type")
    public R<Void> editType(@Valid @RequestBody SysDictType type) {
        dictTypeService.updateById(type);
        return R.ok();
    }

    @Operation(summary = "删除字典类型")
    @SaCheckPermission("system:dict:remove")
    @OperLog(module = "字典管理", type = BusinessType.DELETE)
    @DeleteMapping("/type/{ids}")
    public R<Void> removeType(@PathVariable List<Long> ids) {
        dictTypeService.removeByIds(ids);
        return R.ok();
    }

    /* ---------- 字典数据 ---------- */

    @Operation(summary = "根据字典类型查询字典数据（下拉数据源，无需权限）")
    @GetMapping("/data/{dictType}")
    public R<List<SysDictData>> dataByType(@PathVariable String dictType) {
        return R.ok(dictDataService.selectByDictType(dictType));
    }

    @Operation(summary = "分页查询字典数据")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/data/page")
    public R<PageResult<SysDictData>> dataPage(PageQuery query, String dictType) {
        Page<SysDictData> page = dictDataService.page(new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<SysDictData>()
                        .eq(dictType != null, SysDictData::getDictType, dictType)
                        .orderByAsc(SysDictData::getDictSort));
        return R.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @Operation(summary = "新增字典数据")
    @SaCheckPermission("system:dict:add")
    @OperLog(module = "字典管理", type = BusinessType.INSERT)
    @PostMapping("/data")
    public R<Void> addData(@Valid @RequestBody SysDictData data) {
        dictDataService.save(data);
        return R.ok();
    }

    @Operation(summary = "修改字典数据")
    @SaCheckPermission("system:dict:edit")
    @OperLog(module = "字典管理", type = BusinessType.UPDATE)
    @PutMapping("/data")
    public R<Void> editData(@Valid @RequestBody SysDictData data) {
        dictDataService.updateById(data);
        return R.ok();
    }

    @Operation(summary = "删除字典数据")
    @SaCheckPermission("system:dict:remove")
    @OperLog(module = "字典管理", type = BusinessType.DELETE)
    @DeleteMapping("/data/{ids}")
    public R<Void> removeData(@PathVariable List<Long> ids) {
        dictDataService.removeByIds(ids);
        return R.ok();
    }
}
