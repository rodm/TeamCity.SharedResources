package jetbrains.buildServer.sharedResources.model;

import org.jetbrains.annotations.NotNull;

/**
 * Named lock implementation
 *
 * @author Oleg Rybak
 */
public class Lock {

  /**
   * Name of the lock
   */
  private final String myName;

  /**
   * Type of the lock
   */
  private final LockType myType;

  public Lock(@NotNull String name, @NotNull LockType type) {
    myName = name;
    myType = type;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public LockType getType() {
    return myType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Lock)) return false;
    Lock lock = (Lock) o;
    return myName.equals(lock.myName) && myType == lock.myType;
  }

  @Override
  public int hashCode() {
    int result = myName.hashCode();
    result = 31 * result + myType.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Lock {" +
            "name ='" + myName + '\'' +
            ", type ='" + myType.getName() + '\'' +
            '}';
  }
}