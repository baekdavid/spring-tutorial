package controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import spring.DuplicateMemberDaoException;
import spring.MemberRegisterService;
import spring.RegisterRequest;

import javax.validation.Valid;

@Controller
public class RegisterController {

  private MemberRegisterService memberRegisterService;

  public void setMemberRegisterService(MemberRegisterService memberRegisterService) {
    this.memberRegisterService = memberRegisterService;
  }

  @RequestMapping("/register/step1")
  public String handleStep1() {
    return "register/step1";
  }

  @PostMapping("/register/step2")
  public String handleStep2(
      @RequestParam(value = "agree") Boolean agree,
      Model model) {
    if (!agree) {
      return "register/step1";
    }
    model.addAttribute("registerRequest", new RegisterRequest());
    return "register/step2";
  }

  @GetMapping("/register/step2")
  public String handleStep2Get() {
    return "redirect:/register/step1";
  }

  @PostMapping("/register/step3")
  public String handleStep3(@Valid RegisterRequest regReq, Errors errors) {
    if (errors.hasErrors())
      return "register/step2";
    try {
      memberRegisterService.regist(regReq);
      return "register/step3";
    } catch (DuplicateMemberDaoException ex) {
      errors.rejectValue("email", "duplicate");
      return "register/step2";
    }
  }

//  // 어떤 Validator가 커맨드 객체를 검증할지를 정의한다.
//  @InitBinder
//  protected void initBinder(WebDataBinder binder) {
//    // 컨트롤러 범위에 적용할 Validator 를 설정한다.
//    binder.setValidator(new RegisterRequestValidator());
//  }

}
