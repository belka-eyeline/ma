package mobi.eyeline.utils.mx.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;

public class MBeanOperationModel extends MBeanOperationBriefModel {

  @JsonInclude(ALWAYS)
  private String returnType;

  @JsonInclude(ALWAYS)
  private List<MBeanParameterModel> parameters;

  public String getReturnType() {
    return returnType;
  }

  public MBeanOperationModel setReturnType(String returnType) {
    this.returnType = returnType;
    return this;
  }

  public List<MBeanParameterModel> getParameters() {
    return parameters;
  }

  public MBeanOperationModel setParameters(List<MBeanParameterModel> parameters) {
    this.parameters = parameters;
    return this;
  }

  public MBeanOperationModel fill(final MBeanOperationInfo operation) {
    super.fill(operation);
    setReturnType(operation.getReturnType());
    setParameters(new ArrayList<MBeanParameterModel>() {{
      for (MBeanParameterInfo mBeanParameterInfo : operation.getSignature()) {
        add(new MBeanParameterModel().fill(mBeanParameterInfo));
      }
    }});
    return this;
  }
}
