package com.paultgibbons.fillingbowls;
import java.util.stream.*;
import java.util.*;

public class FillingBowls {
	public static void main(String[] args) {
		List<Integer> l1 = IntStream.of(3, 5).boxed().collect(Collectors.toList());
		List<Integer> l2 = IntStream.of(3, 6).boxed().collect(Collectors.toList());
		List<Integer> l3 = IntStream.of(3, 5, 11, 17).boxed().collect(Collectors.toList());
		System.out.println(canFill(l1, 4)); // true
		System.out.println(canFill(l2, 4)); // false
		System.out.println(canFill(l3, 12)); // true
	}
	
	public static boolean canFill(List<Integer> capacities, int target) {
		List<Bowl> bowls = capacities.stream().map(cap -> new Bowl(cap)).collect(Collectors.toList());
		State initialState = new State(bowls);
		Stack<State> stack = new Stack<>();
		stack.push(initialState);
		Set<State> seen = new HashSet<>();
		seen.add(initialState);
		while(!stack.isEmpty()) {
			List<Bowl> current = stack.pop().bowls;
			if (current.stream().anyMatch(bowl -> {
					return bowl.amount == target;
					})) {
				return true;
			}
			
			current.stream().forEach(bowl -> {
				int orig = bowl.amount;
				// create filling bowl states
				if (bowl.amount < bowl.capacity) {
					bowl.amount = bowl.capacity;
					include(seen, stack, current);
				}
				// create emptying bowl states
				if (bowl.amount > 0) {
					bowl.amount = 0;
					include(seen, stack, current);
				}
				bowl.amount = orig;
			});
			// for each pair a b
			for (int i = 0; i < current.size(); i++) {
				for (int j = 0; j < current.size(); j++) {
					if (i != j) {
						// create state of transferring from a to b
						int aOrig = current.get(i).amount;
						int bOrig = current.get(j).amount;
						current.get(i).transferTo(current.get(j));
						include(seen, stack, current);
						current.get(i).amount = aOrig;
						current.get(j).amount = bOrig;
					}
				}
			}
		}
		return false;
	}
	
	private static class Bowl {
        int capacity;
        int amount;
        public Bowl(int capacity) {
            this.capacity = capacity;
            this.amount = 0;
        }
        public Bowl(Bowl bowl) {
            this.capacity = bowl.capacity;
            this.amount = bowl.amount;
        }
        public void transferTo(Bowl b) {
            if (b.amount + this.amount <= b.capacity) {
                b.amount = b.amount + this.amount;
                this.amount = 0;
            } else {
                this.amount -= (b.capacity - b.amount);
                b.amount = b.capacity;
            }
        }
    }

    private static class State {
        List<Bowl> bowls;
        public State(List<Bowl> bowls) {
            this.bowls = bowls;
        }
        
        @Override
        public int hashCode() {
            int count = 0;
            for (int i = 0; i < this.bowls.size(); i++){
                count += this.bowls.get(i).capacity;
                count += (this.bowls.get(i).capacity * this.bowls.get(i).amount);
                count *= 2;
            }
            return count;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof State)) {
                return false;
            }
            State s2 = (State) o;
            if (!(s2.bowls.size() == this.bowls.size())) {
                return false;
            }
            for (int i = 0; i < this.bowls.size(); i++){
                if (!(this.bowls.get(i).amount == s2.bowls.get(i).amount)) {
                    return false;
                }
                if (!(this.bowls.get(i).capacity == s2.bowls.get(i).capacity)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static void include(Set<State> seen, Stack<State> stack, List<Bowl> current) {
        List<Bowl> newBowls = new ArrayList<>();
        current.forEach(oldBowl -> {
            newBowls.add(new Bowl(oldBowl));
        });
        State s = new State(newBowls);
        if (!seen.contains(s)) {
            seen.add(s);
            stack.push(s);
        }
    }	
}
