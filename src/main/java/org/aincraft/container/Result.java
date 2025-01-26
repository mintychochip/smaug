package org.aincraft.container;

public interface Result {

  enum Status {
    SUCCESS,
    FAILURE
  }

  Status getStatus();

}
