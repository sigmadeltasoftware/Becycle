import Foundation
import common


class BecycleViewModel: ObservableObject {
    @Published var stationList = [Station]()
    
    private let repository: BecycleRepository
    init(repository: BecycleRepository) {
        self.repository = repository
    }
    
    func fetch(network: String) {
        repository.fetchBecycleInfo(network: network) { data, error in
            if let stationList = data {
                self.stationList = stationList
            }
            if let errorReal = error {
               print(errorReal)
            }
        }
    }
}

