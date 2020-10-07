import SwiftUI
import common


struct ContentView : View {
    @ObservedObject var becycleViewModel = BecycleViewModel(repository: BecycleRepository())
    
    var body: some View {
        TabView {
            StationListView(becycleViewModel: becycleViewModel, network: "galway")
                .tabItem {
                    VStack {
                        Image(systemName: "1.circle")
                        Text("Galway")
                    }
                }
            StationListView(becycleViewModel: becycleViewModel, network: "oslo-bysykkel")
                .tabItem {
                    VStack {
                        Image(systemName: "2.circle")
                        Text("Oslo")
                    }
                }
        }
    }
}

struct StationListView: View {
    @ObservedObject var becycleViewModel : BecycleViewModel
    var network: String
    let timer = Timer.publish(every: 30, on: .main, in: .common).autoconnect()
 
    var body: some View {
        NavigationView {
            List(becycleViewModel.stationList, id: \.id) { station in
                StationView(station: station)
            }
            .navigationBarTitle(Text("Auro Portal"))
            .onReceive(timer) { _ in
                self.becycleViewModel.fetch(network: self.network)
            }
            .onAppear(perform: {
                self.becycleViewModel.fetch(network: self.network)
            })
        }
    }
}

struct StationView : View {
    var station: Station

    var body: some View {
        HStack {
            Image("ic_bike").resizable()
                .renderingMode(.template)
                .foregroundColor(station.freeBikes() < 5 ? .orange : .green)
                .frame(width: 32.0, height: 32.0)
            
            Spacer().frame(width: 16)
            
            VStack(alignment: .leading) {
                Text(station.name).font(.headline)
                HStack {
                    Text("Free:").font(.subheadline).frame(width: 80, alignment: .leading)
                    Text("\(station.freeBikes())").font(.subheadline)
                }
                HStack {
                    Text("Slots:").font(.subheadline).frame(width: 80, alignment: .leading)
                    Text("\(station.slots())").font(.subheadline)
                }
            }
        }
    }
}
