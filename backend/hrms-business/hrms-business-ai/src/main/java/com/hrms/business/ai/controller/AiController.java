package com.hrms.business.ai.controller;

import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * AI智能助手控制器
 */
@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI智能助手", description = "AI问答、智能推荐等接口")
public class AiController {

    /**
     * AI问答
     */
    @PostMapping("/chat")
    public Result<Object> chat(@RequestBody Object request) {
        return Result.success();
    }

}
